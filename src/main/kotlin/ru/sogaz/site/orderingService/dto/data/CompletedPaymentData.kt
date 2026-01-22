package ru.sogaz.site.orderingService.dto.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.UUID

data class CompletedPaymentData(
    @field:JsonProperty
    val paymentId: UUID,
    val orderId: UUID,
    val totalAmount: BigDecimal,
    val depersonalization: Boolean = false,
)
