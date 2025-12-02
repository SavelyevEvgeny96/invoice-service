package ru.sogaz.site.orderingService.dto.request

import java.math.BigDecimal
import java.util.UUID

data class PaymentCreatedEvent(
    val eventType: String,
    val timestamp: String,
    val data: PaymentData,
)

data class PaymentData(
    val orderId: UUID?,
    val premiumAmount: BigDecimal?,
    val keyCard: String?,
    val recipientEmail: String?,
    val recipientPhone: String?,
    val dateCreate: String?,
    val dateEnd: String?,
    val bank: String?,
    val paymentType: String?,
)
