package ru.sogaz.site.orderingService.dto.response

import java.util.UUID

data class CreateOrderResult(
    val orderId: UUID,
    val paymentUrl: String,
    val shortPaymentUrl: String? = null,
)
