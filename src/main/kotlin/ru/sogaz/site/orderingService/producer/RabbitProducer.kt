package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import java.util.UUID

abstract class RabbitProducer<T>(
    private val rabbitTemplate: RabbitTemplate,
) {
    protected fun convertAndSend(
        exchange: String,
        routingKey: String,
        message: T,
        correlationId: UUID? = null,
    ): Unit =
        convertAndSend(
            exchange,
            routingKey,
            message!!,
            correlationId,
            null
        )

    protected fun convertAndSend(
        exchange: String,
        routingKey: String,
        message: T,
        messagePostProcessor: MessagePostProcessor,
    ): Unit =
        convertAndSend(
            exchange,
            routingKey,
            message!!,
            null,
            messagePostProcessor,
        )

    protected fun convertAndSend(
        exchange: String,
        routingKey: String,
        message: T,
        correlationId: UUID?,
        messagePostProcessor: MessagePostProcessor?,
    ): Unit =
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            message!!,
            {
                it?.apply {
                    messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                    messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                    messageProperties.headers.remove("__TypeId__")
                    messagePostProcessor?.postProcessMessage(this)
                }
            },
            correlationId?.let { CorrelationData(it.toString()) },
        )
}
