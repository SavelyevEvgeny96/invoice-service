package ru.sogaz.site.orderingService.dto.data

import java.time.Instant
import java.util.UUID

class SentReceiptData(
    val receiptId: UUID,
    val orderId: UUID?,
    val paymentId: UUID,
    val amount: String,
    val state: String,
    val typeOperation: String,
    val sendingTime: Instant?,
    val link: String?,
    val errorText: String?,
)
