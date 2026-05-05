package ru.sogaz.site.orderingService.producer

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface InvoicePaymentStatusRegEventProducer {
    fun sendPaymentStatusRegEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    )
}
