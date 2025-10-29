package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.orderingService.dto.data.PublishResult
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.PaymentEventProducer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaymentEventProducerImpl(
    private val rabbit: RabbitTemplate,
    private val props: RabbitProps,
) : PaymentEventProducer {
    companion object {
        private const val NO_CONFIRM_LOG = "Нет подтверждения на данный момент: orderId=%s"
        private const val EMPTY_BATCH_LOG = "Пустая пачка событий — отправка пропущена"
        private const val PUBLISHED_LOG = "Отправлено сообщение для orderId=%s, eventType=%s"
        private const val PUBLISH_ERROR_LOG = "Ошибка при отправке сообщения orderId=%s: %s"
        private const val BATCH_RESULT_LOG = "Результат отправки: подтверждено=%d, ошибок=%d, неподтверждено=%d"
    }

    private val logger = loggerFor(PaymentEventProducerImpl::class.java)

    override fun sendBatch(events: List<PaymentCreatedEvent>): PublishResult {
        if (events.isEmpty()) {
            logger.debug(EMPTY_BATCH_LOG)
            return PublishResult(emptySet(), emptyMap(), emptySet())
        }
        val errors = ConcurrentHashMap<UUID, String?>()
        val confirmed = ConcurrentHashMap<UUID, Boolean>()
        val acked = mutableSetOf<UUID>()
        val nAcked = mutableMapOf<UUID, String?>()
        val unconfirmed = mutableSetOf<UUID>()

        events.forEach { event ->
            val orderId = event.data.orderId
            val correlationData = CorrelationData(orderId.toString())

            try {
                rabbit.convertAndSend(
                    props.paymentsExchange,
                    event.eventType,
                    event,
                    { msg ->
                        msg.messageProperties.correlationId = orderId.toString()
                        msg
                    },
                    correlationData,
                )
                logger.debug(PUBLISHED_LOG.format(orderId, event.eventType))
            } catch (ex: Exception) {
                logger.error(PUBLISH_ERROR_LOG.format(orderId, ex.message))
                nAcked[orderId] = ex.message
            }

            when {
                confirmed.remove(orderId) == true -> acked += orderId
                errors.containsKey(orderId) -> nAcked[orderId] = errors.remove(orderId)
                else -> {
                    logger.warn(NO_CONFIRM_LOG.format(orderId))
                    unconfirmed += orderId
                }
            }
        }

        logger.info(BATCH_RESULT_LOG.format(acked.size, nAcked.size, unconfirmed.size))
        return PublishResult(acked, nAcked, unconfirmed)
    }
}
