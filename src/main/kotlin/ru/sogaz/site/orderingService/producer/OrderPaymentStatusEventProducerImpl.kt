package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.PaidOrderMessage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.mappers.order.PaidOrderMessagesMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Component("orderStatusEventProducer")
class OrderPaymentStatusEventProducerImpl(
    rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val paidOrderMessagesMapper: PaidOrderMessagesMapper,
    private val invoicePaymentStatusRegEventProducer: InvoicePaymentStatusRegEventProducer,
    private val sendMessageProducer: SendMessageProducer
) : RabbitProducer<PaidOrderMessage>(rabbitTemplate),
    OrderPaymentStatusEventProducer {
    companion object {
        private const val EMPTY_ROUTING_KEY_ERROR_MESSAGE =
            "Для заказа не указана целевая очередь для отправки статуса оплаты"
    }

    override fun sendPaymentOrderEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) {

        val rk = if (completedPaymentData.operationType == OperationTypeEnum.REVERSAL) {
            invoicePaymentStatusRegEventProducer.buildRoutingKey(
                order,
                completedPaymentData
            )
        } else {
            order.queueStatusResultName
        }
        sendMessageProducer.sendMessage(
            rk,
            paidOrderMessagesMapper.toPaidOrderMessage(order, completedPaymentData),
            rabbitProps.ordersExchange,
            order.orderId,
        )
    }

}
