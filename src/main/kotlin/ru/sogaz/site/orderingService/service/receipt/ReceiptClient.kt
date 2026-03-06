package ru.sogaz.site.orderingService.service.receipt

import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest

interface ReceiptClient {
    fun sendReceiptToQueue(receiptCreateRequest: PaymentReceiptCreateRequest)
}
