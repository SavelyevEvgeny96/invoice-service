package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.response.InvoiceStatusEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.mappers.order.InvoiceStatusMapper
import ru.sogaz.site.orderingService.mappers.order.InvoiceStatusRegMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Component("invoiceStatusEventProducer")
class InvoicePaymentStatusEventProducerImpl(
    private val rabbitProps: RabbitProps,
    private val eventMapper: InvoiceStatusMapper,
    private val invoiceStatusRegMapper: InvoiceStatusRegMapper,
    private val sendMessageProducer: SendMessageProducer,
) : OrderPaymentStatusEventProducer,
    InvoicePaymentStatusRegEventProducer {
    companion object {
        private val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
        private const val ROUTING_KEY_PREFIX = "invoice.v2"
        private const val DOT = "."
    }

    override fun sendPaymentOrderEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) {
        sendMessageProducer.sendMessage(
            buildRoutingKey(order, completedPaymentData),
            eventMapper.toInvoiceStatusEvent(order, completedPaymentData),
            rabbitProps.ordersExchange,
            order.orderId,
        )
    }

    override fun sendPaymentStatusRegEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) = sendMessageProducer.sendMessage(
        order.queueStatusResultName,
        invoiceStatusRegMapper.toInvoiceStatusRegEvent(order, completedPaymentData),
        rabbitProps.ordersExchange,
        order.orderId,
    )

    override fun buildRoutingKey(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): String {
        val operation = completedPaymentData.operationType.name.lowercase()
        val clientId = order.getClientId()
        return buildString {
            append(ROUTING_KEY_PREFIX)
            append(DOT)
            append(operation)
            clientId?.let {
                append(DOT)
                append(it)
            }
        }
    }

    private fun OrderEntity.getClientId() =
        clientId
            ?.takeIf { it.isNotBlank() }
            ?.replace(NON_ALPHANUMERIC_REGEX, DOT)
}
