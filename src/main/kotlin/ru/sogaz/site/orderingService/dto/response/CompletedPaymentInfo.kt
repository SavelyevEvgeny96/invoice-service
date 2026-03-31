package ru.sogaz.site.orderingService.dto.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CompletedPaymentInfo(
    val invoiceId: UUID,
    val payerIP: String?,
    val paymentDate: Instant?,
    val amount: BigDecimal,
    val bank: String?,
    val paymentMethod: String?,
    val paymentBankId: String?,
    val cardNumber: String?,
    val listReceipts: List<SentReceiptInfo>,
)

data class SentReceiptInfo(
    val paymentId: UUID,
    val receiptId: UUID,
    val sendingTime: Instant?,
    val amount: BigDecimal,
    val typeOperation: String?,
    val receiptSendingStatus: String?,
    val link: String?,
)
