package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

data class CreateOrderDataV1(
    val orderId: UUID,
    val url: String,
)
