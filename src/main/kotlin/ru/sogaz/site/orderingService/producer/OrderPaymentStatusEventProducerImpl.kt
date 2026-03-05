package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
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
        completedPaymentData: CompletedPaymentData,
    ) = rabbitTemplate.convertAndSend(
        rabbitProps.ordersExchange,
        requireNotNull(order.queueStatusResultName) { EMPTY_ROUTING_KEY_ERROR_MESSAGE },
        paidOrderMessagesMapper.toPaidOrderMessage(order, completedPaymentData),
        {
            it.apply {
                messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = rabbitProps.ordersExchange
                messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = order.queueStatusResultName
            }
        },
        CorrelationData(order.orderId.toString()),
    )
}
