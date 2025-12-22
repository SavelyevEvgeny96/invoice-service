package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.PublishResult
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.PaymentEventProducer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PaymentEventProducerImpl(
    private val rabbit: RabbitTemplate,
    private val props: RabbitProps,
    private val sendMessageProducer: SendMessageProducer
) : PaymentEventProducer {
    companion object {
        private const val NO_CONFIRM_LOG = "Нет подтверждения об ошибке на данный момент: orderId=%s"
        private const val PUBLISHED_LOG = "Отправлено сообщение для orderId=%s"
        private const val PUBLISH_ERROR_LOG = "Ошибка при отправке сообщения orderId=%s: %s"
        private const val BATCH_RESULT_LOG = "Результат отправки: подтверждено=%d, ошибок=%d, неподтверждено=%d"
    }

    private val logger = loggerFor(PaymentEventProducerImpl::class.java)

    override fun sendBatchOrderCreated(events: List<OrderPayloadDto>): PublishResult {
        val errors = ConcurrentHashMap<UUID?, String?>()
        val confirmed = ConcurrentHashMap<UUID?, Boolean>()
        val acked = mutableSetOf<UUID?>()
        val nAcked = mutableMapOf<UUID?, String?>()
        val unconfirmed = mutableSetOf<UUID?>()

        events.forEach { event ->
            val orderIdRecurrent = event.orderIdRecurrent
            try {
                sendMessageProducer.sendMessage(
                    props.routingKeyPayment,
                    event,
                    props.paymentsExchange,
                    orderIdRecurrent
                )
                logger.debug(PUBLISHED_LOG.format(orderIdRecurrent))
            } catch (ex: Exception) {
                logger.error(PUBLISH_ERROR_LOG.format(orderIdRecurrent, ex.message))
                nAcked[orderIdRecurrent] = ex.message
            }

            when {
                confirmed.remove(orderIdRecurrent) == true -> acked += orderIdRecurrent
                errors.containsKey(orderIdRecurrent) -> nAcked[orderIdRecurrent] = errors.remove(orderIdRecurrent)
                else -> {
                    logger.warn(NO_CONFIRM_LOG.format(orderIdRecurrent))
                    unconfirmed += orderIdRecurrent
                }
            }
        }

        logger.info(BATCH_RESULT_LOG.format(acked.size, nAcked.size, unconfirmed.size))
        return PublishResult(acked, nAcked, unconfirmed)
    }
}
