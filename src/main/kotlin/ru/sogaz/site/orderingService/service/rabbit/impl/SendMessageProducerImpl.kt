package ru.sogaz.site.orderingService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.enums.RefundErrorReason
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import ru.sogaz.site.orderingService.service.rabbit.impl.OrderBatchConsumerImpl.Companion.AUTHOR_REGEX
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Producer (отправитель) сообщений в RabbitMQ для сценария Refund.
 *
 * Основная ответственность класса:
 * 1) Разобрать результат подготовки (RefundPreparationResult) на группы (missing / noAccess / notPaidFor / found).
 * 2) Для каждой группы сформировать DTO (ошибка или успех) и отправить его в RabbitMQ.
 *
 * Ключевые детали:
 * - Для каждой записи из результата мы определяем routing key из dto.routingKeyStatus.
 * - Ошибки маппим через RefundErrorMapper (единая логика формирования ошибок).
 * - Успешный ответ собираем как RefundPayloadDto.
 * - Публикация в RabbitMQ делается через RabbitTemplate.convertAndSend.
 * - В сообщение добавляются технические headers для трассировки (author, flowCode, timestamp и т.п.).
 *
 * Важно:
 * - Сообщения отправляются по всем элементам батча.
 * - CorrelationData может использоваться для correlation/confirm-логики (если включены publisher confirms).
 */
@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val objectMapper: ObjectMapper,
    private val refundErrorMapper: RefundErrorMapper,
    private val queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService,
) : SendMessageProducer {
    private val logger = loggerFor(OrderBatchConsumerImpl::class.java)

    /**
     * Отправляет сообщения по результатам подготовки refund-заказов.
     *
     * На вход получает результат, который уже содержит разнесение по сценариям:
     * - missing     — ордер не найден в БД
     * - noAccess    — нет прав/доступа у author
     * - notForPaid  — ордер существует, но не оплачен (или статус не SUCCESS)
     * - found       — ордер найден, доступ есть, статус корректный -> отправляем успех
     *
     * Для каждой записи:
     * - Формируем ответный DTO (error/success)
     * - Отправляем в exchange
     *
     * @param resultOrder Результат подготовки, содержащий сгруппированные элементы
     */
    override fun sendMessageRefund(resultOrder: RefundPreparationResult) {
        // 1) Ошибочные группы сводим в одну мапу "причина -> список"
        val errorBatches: Map<RefundErrorReason, List<RefundPayloadDto>> =
            mapOf(
                RefundErrorReason.ORDER_NOT_FOUND to resultOrder.missing,
                RefundErrorReason.NOT_PAID_FOR to resultOrder.notForPaid,
                RefundErrorReason.NO_ACCESS to resultOrder.noAccess,
            )

        // 2) Обрабатываем все ошибки одинаково
        errorBatches.forEach { (reason, batch) ->
            batch.forEach { item ->
                val rk = item.routingKeyStatus.orEmpty()
                val errorDto = refundErrorMapper.toErrorDto(item, reason)
                sendMessage(rk, errorDto, rabbitProps.ordersExchange, item.orderId)
            }
        }

        // 3) Успех отдельно (тут другой DTO)
        resultOrder.found.forEach { item ->
            val successDto =
                RefundPayloadDto(
                    item.metaInfo,
                    item.orderId,
                    null,
                    item.premiumAmount,
                    item.description,
                )
            sendMessage(rabbitProps.routingKeyRefundPayment, successDto, rabbitProps.paymentsExchange, item.orderId)
        }
    }

    /**
     * Обрабатывает битое сообщение, из которого удалось извлечь {@code author}.
     *
     * <p>Метод предназначен для сообщений, которые:
     * <ul>
     *     <li>не соответствуют целевому DTO</li>
     *     <li>не должны попадать в DLQ</li>
     *     <li>должны быть перенаправлены во внешнюю техническую очередь
     *         для дальнейшего анализа или ручной обработки</li>
     * </ul>
     *
     * <p><b>Алгоритм обработки:</b>
     * <ol>
     *     <li>Формируется routing key на основе {@code author}.</li>
     *     <li>Сообщение переотправляется в указанный exchange в «сыром» виде
     *         (без сериализации).</li>
     *     <li>Используется publisher confirms для гарантии доставки.</li>
     *     <li>Только после подтверждения брокером выполняется
     *         {@link Channel#basicAck(long, boolean)} для исходного сообщения.</li>
     * </ol>
     *
     * <p><b>Поведение при ошибках:</b>
     * <ul>
     *     <li>Если отправка или confirm завершаются ошибкой — ACK не выполняется.</li>
     *     <li>Сообщение остаётся unacked и будет переотправлено RabbitMQ.</li>
     * </ul>
     *
     * @param errorParsed результат парсинга битого сообщения
     * @param channel     RabbitMQ channel, используемый для publish и ACK
     * @param exchange    exchange, в который отправляется битое сообщение
     */
    override fun <T : Any> processErrorMessages(
        errorParsed: ParsedResult.Error<T>,
        channel: Channel,
        exchange: String,
        statusPattern: String,
    ) {
        try {
            // Логируем битое сообщение для трассировки
            logger.warn(
                "Битое сообщение от автора=${errorParsed.author}: ${errorParsed.rawMessage}",
            )

            // Формируем routing key для технической очереди
            val rKey =
                queueStatusResultNameNormalizeService
                    .buildQueueStatusResultName(
                        statusPattern,
                        errorParsed.author,
                    )

            // Переотправляем raw payload с подтверждением от брокера
            sendRawMessageWithConfirm(
                channel,
                exchange,
                rKey,
                errorParsed.rawMessage,
            )

            // ACK исходного сообщения выполняется
            // ТОЛЬКО после успешного publisher confirm
            channel.basicAck(errorParsed.tag, false)
        } catch (ex: Exception) {
            logger.error(
                "Ошибка при обработке сообщения ${errorParsed.tag} от автора=${errorParsed.author}",
                ex,
            )
            // ACK не выполняем → сообщение останется unacked
            // и будет переотправлено RabbitMQ
        }
    }

    /**
     * Отправляет сообщение в RabbitMQ в «сыром» виде (без сериализации payload)
     * с использованием publisher confirms.
     *
     * <p>Метод предназначен для переотправки уже сформированного тела сообщения,
     * например:
     * <ul>
     *     <li>битого или неполного JSON</li>
     *     <li>сообщений, полученных из RabbitMQ и пересылаемых дальше</li>
     *     <li>форензики, технических очередей, внешней обработки</li>
     * </ul>
     *
     * <p><b>Особенности:</b>
     * <ul>
     *     <li>Сообщение отправляется как {@code byte[]} без участия
     *         {@link org.springframework.amqp.support.converter.MessageConverter}.</li>
     *     <li>Payload сохраняется в исходном виде (без экранирования и double-encoding).</li>
     *     <li>Используется синхронное ожидание publisher confirm от брокера.</li>
     * </ul>
     *
     * <p><b>Алгоритм:</b>
     * <ol>
     *     <li>Формируются базовые {@link AMQP.BasicProperties}
     *         (contentType, deliveryMode).</li>
     *     <li>Включается режим publisher confirms для канала.</li>
     *     <li>Сообщение публикуется через {@link Channel#basicPublish}.</li>
     *     <li>Метод блокируется до получения подтверждения от брокера.</li>
     *     <li>При отсутствии подтверждения выбрасывается исключение.</li>
     * </ol>
     *
     * <p><b>Гарантии:</b><br>
     * Метод либо завершается успешно (сообщение подтверждено брокером),
     * либо выбрасывает исключение, позволяя вызывающему коду
     * не выполнять ACK исходного сообщения.
     *
     * @param channel    RabbitMQ channel, используемый для publish и confirms
     * @param exchange   exchange, в который публикуется сообщение
     * @param routingKey routing key для маршрутизации сообщения
     * @param rawBody    тело сообщения в виде строки (отправляется без сериализации)
     *
     * @throws RuntimeException если брокер не подтвердил публикацию
     */
    override fun sendRawMessageWithConfirm(
        channel: Channel,
        exchange: String,
        routingKey: String,
        rawBody: String,
    ) {
        // Минимальный набор AMQP properties
        val props =
            AMQP.BasicProperties
                .Builder()
                .contentType("application/json")
                .deliveryMode(2) // persistent message
                .build()

        // Включаем publisher confirms для канала
        channel.confirmSelect()

        // Публикуем raw payload без сериализации
        channel.basicPublish(
            exchange,
            routingKey,
            props,
            rawBody.toByteArray(Charsets.UTF_8),
        )

        // Синхронно ожидаем подтверждения от брокера
        if (!channel.waitForConfirms(3000)) {
            throw RuntimeException("Сообщение не подтверждено брокером")
        }
    }

    /**
     * Универсальный метод отправки сообщения в RabbitMQ.
     *
     * Метод generic, чтобы не использовать `Any` и сохранить тип payload на уровне Kotlin.
     * Фактическая сериализация всё равно выполняется Spring AMQP `MessageConverter` (обычно Jackson).
     *
     * Что делает метод:
     * 1) Генерирует timestamp для headers.
     * 2) Создаёт `CorrelationData` для трассировки / publisher confirms.
     * 3) Отправляет сообщение в exchange + routingKey.
     * 4) Дополняет message headers технической информацией.
     *
     * Важные поля:
     * - `messageProperties.correlationId` — correlation id внутри message properties
     * - `CorrelationData`               — отдельная структура Spring для корреляции confirm'ов
     *
     * @param routingKey routing key, по которому маршрутизируем сообщение
     * @param payload объект полезной нагрузки (DTO)
     * @param exchange exchange, в который отправляем сообщение
     * @param orderId бизнес-корреляция (если null — создаём случайный correlation id)
     */
    override fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: UUID?,
    ) {
        // --- 1) Формируем timestamp в UTC для заголовков ---
        val timestamp =
            OffsetDateTime
                .now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        // --- 2) Формируем correlationId для confirm/trace ---
        // Если orderId null — используем случайный UUID, чтобы correlation data не было "null".
        val correlationId = orderId?.toString() ?: UUID.randomUUID().toString()

        // CorrelationData — объект Spring AMQP для publisher confirms и корреляции.
        val cd = CorrelationData(correlationId)

        // --- 3) Публикация сообщения ---
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            payload,
            // --- 4) MessagePostProcessor: дополняем заголовки/свойства перед отправкой ---
            { message ->

                // 4.1) Технические headers для трассировки и диагностики
                message.messageProperties.headers["author"] = "payService"
                message.messageProperties.headers["flowCode"] = "ResultPay"
                message.messageProperties.headers["timestamp"] = timestamp

                // 4.2) Пробрасываем exchange/routingKey в headers (удобно для логов/трассировки)
                message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey

                // 4.3) CorrelationId внутри message properties (может использоваться consumer’ом)
                message.messageProperties.correlationId = orderId?.toString()
                message.messageProperties.headers.remove("__TypeId__")

                message
            },
            cd,
        )
    }

    override fun <T : Any> parseBatch(
        messages: List<Message>,
        channel: Channel,
        dtoClass: Class<T>,
    ): List<ParsedResult<T>> {
        val result = mutableListOf<ParsedResult<T>>()

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            val messageId = msg.messageProperties.messageId
            val body = String(msg.body, Charsets.UTF_8)
            try {
                val dto = objectMapper.readValue(body, dtoClass)
                result += ParsedResult.Success(tag, dto, messageId)
            } catch (ex: Exception) {
                val author = extractAuthorUnsafe(body)
                if (author != null) {
                    // Сообщение битое, передаём в handleBatch для обработки
                    result += ParsedResult.Error(tag, body, author, messageId)
                } else {
                    // author не нашли → реджектим один раз
                    try {
                        channel.basicReject(tag, false)
                    } catch (ackEx: Exception) {
                        logger.error("Не удалось сделать basicReject для tag=$tag", ackEx)
                    }
                }
            }
        }

        return result
    }

    override fun extractAuthorUnsafe(body: String): String? {
        // 1. Пытаемся по-человечески
        runCatching {
            val node = objectMapper.readTree(body)
            val json =
                if (node.isTextual) objectMapper.readTree(node.asText()) else node

            return json
                .path("metaInfo")
                .firstOrNull()
                ?.path("author")
                ?.asText()
        }

        // 2. Fallback — режем строку
        return AUTHOR_REGEX
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
    }
}
