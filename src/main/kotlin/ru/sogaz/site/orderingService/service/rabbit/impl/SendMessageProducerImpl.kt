package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
) : SendMessageProducer {
    private val logger = loggerFor(SendMessageProducerImpl::class.java)

    companion object {
        const val START_LOG_MESSAGE_QUEUE = "Старт записи в очередь routingKey: %s  exchange: %s "
    }

    override fun sendMessage(
        routingKey: String,
        paidOrderMessage: Any,
        exchange: String,
        orderId: UUID?,
    ) {
        val timestamp =
            OffsetDateTime
                .now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        logger.info(
            START_LOG_MESSAGE_QUEUE.format(
                routingKey,
                exchange,
            ),
        )
        val cd = CorrelationData(orderId.toString())
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            paidOrderMessage,
            { message ->
                message.messageProperties.headers["author"] = "payService"
                message.messageProperties.headers["flowCode"] = "ResultPay"
                message.messageProperties.headers["timestamp"] = timestamp
                message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                message.messageProperties.correlationId = orderId.toString()
                message
            },
            cd
        )
    }
}
