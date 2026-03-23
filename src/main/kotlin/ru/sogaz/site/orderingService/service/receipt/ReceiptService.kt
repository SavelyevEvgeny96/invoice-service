package ru.sogaz.site.orderingService.service.receipt

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.SentReceiptData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.ReceiptEntity

interface ReceiptService {
    fun sendReceipt(completedPaymentData: CompletedPaymentData): OrderEntity

    fun saveSentReceiptRecord(sentReceiptData: SentReceiptData): ReceiptEntity
}
