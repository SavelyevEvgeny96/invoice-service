package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.enums.RefundErrorReason
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.PAYMENT_STATUS_PATTERN
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
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
                )
            sendMessage(rabbitProps.routingKeyRefundPayment, successDto, rabbitProps.paymentsExchange, item.orderId)
        }
    }

    override fun processErrorMessages(
        errorParsed: ParsedResult.Error<OrderPayloadDto>,
        channel: Channel,
        exchange: String,
    ) {
        try {
            logger.warn("Битое сообщение от автора=${errorParsed.author}: ${errorParsed.rawMessage}")

            val rKey =
                queueStatusResultNameNormalizeService
                    .buildQueueStatusResultName(PAYMENT_STATUS_PATTERN, errorParsed.author)

            // Отправка с подтверждением
            sendRawMessageWithConfirm(channel, exchange, rKey, errorParsed.rawMessage)

            // Только после успешного подтверждения делаем ack
            channel.basicAck(errorParsed.tag, false)
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке сообщения ${errorParsed.tag} от автора=${errorParsed.author}", ex)
            // не делаем basicAck, сообщение вернется в очередь
        }
    }

    /**
     * Отправляет сообщение в RabbitMQ БЕЗ сериализации payload.
     *
     * <p>
     * В отличие от {@code sendConvertedMessage}, данный метод предназначен
     * для отправки уже готового тела сообщения (raw payload), например:
     * <ul>
     *   <li>битого или неполного JSON</li>
     *   <li>сообщений, полученных из RabbitMQ и переотправляемых дальше</li>
     *   <li>форензики / DLQ / технических очередей</li>
     * </ul>
     *
     * <p>
     * Метод НЕ использует {@link org.springframework.amqp.support.converter.MessageConverter}
     * и отправляет сообщение как массив байт, сохраняя тело в исходном виде
     * (без экранирования, без double-encoding).
     *
     * <p>
     * Ответственность за корректность формата payload (JSON / text / broken JSON)
     * полностью лежит на вызывающей стороне.
     *
     * <p>
     * Что делает метод:
     * <ol>
     *   <li>Формирует технические headers (author, flowCode, timestamp)</li>
     *   <li>Устанавливает {@code contentType=application/json} и {@code UTF-8 encoding}</li>
     *   <li>Устанавливает {@code correlationId} для трассировки и publisher confirms</li>
     *   <li>Отправляет сообщение через {@link org.springframework.amqp.rabbit.core.RabbitTemplate#send}</li>
     * </ol>
     *
     * @param routingKey routing key, по которому маршрутизируется сообщение
     * @param rawBody тело сообщения в виде строки (отправляется без сериализации)
     * @param exchange exchange, в который публикуется сообщение
     * @param orderId бизнес-корреляция сообщения (если {@code null}, используется случайный UUID)
     */
    override fun sendRawMessageWithConfirm(
        channel: Channel,
        exchange: String,
        routingKey: String,
        rawBody: String,
    ) {
        val props =
            AMQP.BasicProperties
                .Builder()
                .contentType("application/json")
                .deliveryMode(2) // persistent
                .build()

        channel.confirmSelect() // включаем подтверждения
        channel.basicPublish(exchange, routingKey, props, rawBody.toByteArray(Charsets.UTF_8))

        // Ждем синхронно подтверждения брокера
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

                message
            },
            cd,
        )
    }
}
