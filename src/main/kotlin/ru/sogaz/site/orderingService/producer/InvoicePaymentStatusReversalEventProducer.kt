package ru.sogaz.site.orderingService.producer

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface InvoicePaymentStatusReversalEventProducer {
    fun sendPaymentStatusReversalEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    )

}