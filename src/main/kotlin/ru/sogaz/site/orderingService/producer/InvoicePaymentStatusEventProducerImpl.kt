package ru.sogaz.site.orderingService.producer

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.loggerFor
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
    private val orderDao: OrderDao,
) : OrderPaymentStatusEventProducer,
    InvoicePaymentStatusRegEventProducer {
    companion object {
        private val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
        private const val ROUTING_KEY_PREFIX = "invoice.v2"
        private const val DOT = "."
    }

    private val logger = loggerFor(InvoicePaymentStatusEventProducerImpl::class.java)

    @Transactional
    override fun sendPaymentOrderEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) {
        if (order.sendMessageResult == true) {
            logger.warn(
                "Предотвращена попытка двойной отправки статуса оплаты. " +
                    "Order orderId={} уже имеет sendMessageResult=true",
                order.orderId,
            )
            return
        }

        val adjustedPaymentData =
            completedPaymentData.let { data ->
                data.takeUnless { order.checkPaymentInformation == true }?.copy(rrn = null) ?: data
            }

        sendMessageProducer.sendMessage(
            buildRoutingKey(order, completedPaymentData),
            eventMapper.toInvoiceStatusEvent(order, adjustedPaymentData),
            rabbitProps.ordersExchange,
            order.orderId,
        )
        order.sendMessageResult = true
        orderDao.save(order)
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
