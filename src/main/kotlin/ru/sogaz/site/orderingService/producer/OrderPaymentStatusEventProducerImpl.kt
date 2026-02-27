package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.order.PaidOrderMessagesMapper
import ru.sogaz.site.orderingService.properties.RabbitProps

@Component
class OrderPaymentStatusEventProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val paidOrderMessagesMapper: PaidOrderMessagesMapper,
) : OrderPaymentStatusEventProducer {
    companion object {
        private const val EMPTY_ROUTING_KEY_ERROR_MESSAGE = "Для заказа не указана целевая очередь для отправки статуса оплаты"
    }

    override fun sendPaymentOrderEvent(
        order: OrderEntity,
        errorText: String?,
    ) = rabbitTemplate.convertAndSend(
        rabbitProps.ordersExchange,
        requireNotNull(order.queueStatusResultName) { EMPTY_ROUTING_KEY_ERROR_MESSAGE },
        paidOrderMessagesMapper.toPaidOrderMessage(order, errorText),
        CorrelationData(order.orderId.toString()),
    )
}
