package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.data.ParsedData
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.dto.data.RefundSuccessDto
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto
import ru.sogaz.site.orderingService.enums.RefundErrorReason
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Producer (отправитель) сообщений в RabbitMQ для сценария Refund.
 *
 * Основная ответственность класса:
 * 1) Разобрать результат подготовки (`RefundPreparationResult`) на группы (missing / noAccess / notPaidFor / found).
 * 2) Для каждой группы сформировать DTO (ошибка или успех) и отправить его в RabbitMQ.
 * 3) Подтвердить (ack) сообщение в RabbitMQ через `Channel.basicAck()` только после обработки конкретного входного сообщения.
 *
 * Ключевые детали:
 * - Для каждой записи из результата мы определяем routing key из `dto.routingKeyStatus`.
 * - Ошибки маппим через `RefundErrorMapper` (единая логика формирования ошибок).
 * - Успешный ответ собираем как `RefundSuccessDto`.
 * - Публикация в RabbitMQ делается через `RabbitTemplate.convertAndSend`.
 * - В сообщение добавляются технические headers для трассировки (author, flowCode, timestamp и т.п.).
 *
 * Важно:
 * - `basicAck` вызывается после отправки сообщения. Если отправка упадёт исключением — ack не произойдёт,
 *   и сообщение можно будет пере обработать (в зависимости от настроек consumer/requeue).
 * - `CorrelationData` используется для correlation/confirm-логики (если включены publisher confirms).
 */
@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val refundErrorMapper: RefundErrorMapper,
) : SendMessageProducer {
    /**
     * Отправляет сообщения по результатам подготовки refund-заказов.
     *
     * На вход получает результат, который уже содержит разнесение по сценариям:
     * - `missing`     — ордер не найден в БД
     * - `noAccess`    — нет прав/доступа у author
     * - `notForPaid`  — ордер существует, но не оплачен (или статус не SUCCESS)
     * - `found`       — ордер найден, доступ есть, статус корректный -> отправляем успех
     *
     * Для каждой записи:
     * - Формируем ответный DTO (error/success)
     * - Отправляем в exchange
     * - Ack исходного сообщения по `tag` (подтверждаем брокеру, что оно обработано)
     *
     * @param resultOrder Результат подготовки, содержащий сгруппированные элементы
     * @param channel RabbitMQ Channel, через который делается manual-ack входных сообщений
     */
    override fun sendMessageRefund(
        resultOrder: RefundPreparationResult,
        channel: Channel,
    ) {
        // 1) Ошибочные группы сводим в одну мапу "причина -> список"
        val errorBatches: Map<RefundErrorReason, List<ParsedData<RefundPayloadDto>>> =
            mapOf(
                RefundErrorReason.ORDER_NOT_FOUND to resultOrder.missing,
                RefundErrorReason.NOT_PAID_FOR to resultOrder.notForPaid,
                RefundErrorReason.NO_ACCESS to resultOrder.noAccess,
            )

        // 2) Обрабатываем все ошибки одинаково
        errorBatches.forEach { (reason, batch) ->
            batch.forEach { item ->
                val payload = item.dto
                val rk = payload.routingKeyStatus.orEmpty()

                val errorDto = refundErrorMapper.toErrorDto(payload, reason)
                sendMessage(rk, errorDto, rabbitProps.ordersExchange, payload.orderId)

                channel.basicAck(item.tag, false)
            }
        }

        // 3) Успех отдельно (тут другой DTO)
        resultOrder.found.forEach { item ->
            val payload = item.dto

            val successDto =
                RefundSuccessDto(
                    payload.metaInfo,
                    payload.orderId,
                    payload.bank,
                )
            sendMessage(rabbitProps.routingKeyRefundPayment, successDto, rabbitProps.paymentsExchange, payload.orderId)

            channel.basicAck(item.tag, false)
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
