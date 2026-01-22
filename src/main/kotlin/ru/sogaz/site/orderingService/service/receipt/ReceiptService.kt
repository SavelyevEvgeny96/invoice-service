package ru.sogaz.site.orderingService.service.receipt

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface ReceiptService {
    fun sendReceipt(completedPaymentData: CompletedPaymentData): OrderEntity
}
