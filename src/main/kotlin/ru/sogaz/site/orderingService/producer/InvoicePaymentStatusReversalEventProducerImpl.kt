package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.order.InvoiceStatusMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
@Service
class InvoicePaymentStatusReversalEventProducerImpl(
    rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val eventMapper: InvoiceStatusMapper,
    private val invoicePaymentStatusRegEventProducer: InvoicePaymentStatusRegEventProducer,
    private val sendMessageProducer: SendMessageProducer,
) : InvoicePaymentStatusReversalEventProducer {
    override fun sendPaymentStatusReversalEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) {
        sendMessageProducer.sendMessage(
            invoicePaymentStatusRegEventProducer.buildRoutingKey(order, completedPaymentData),
            eventMapper.toInvoiceReversalStatusEvent(completedPaymentData),
            rabbitProps.ordersExchange,
            order.orderId,
        )
        return
    }
}
