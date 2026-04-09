package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.PaidOrderMessage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.order.PaidOrderMessagesMapper
import ru.sogaz.site.orderingService.properties.RabbitProps

@Component("orderStatusEventProducer")
class OrderPaymentStatusEventProducerImpl(
    rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val paidOrderMessagesMapper: PaidOrderMessagesMapper,
) : RabbitProducer<PaidOrderMessage>(rabbitTemplate),
    OrderPaymentStatusEventProducer {
    companion object {
        private const val EMPTY_ROUTING_KEY_ERROR_MESSAGE = "Для заказа не указана целевая очередь для отправки статуса оплаты"
    }

    override fun sendPaymentOrderEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) = convertAndSend(
        rabbitProps.ordersExchange,
        requireNotNull(order.queueStatusResultName) { EMPTY_ROUTING_KEY_ERROR_MESSAGE },
        paidOrderMessagesMapper.toPaidOrderMessage(order, completedPaymentData),
        order.orderId,
    )
}
