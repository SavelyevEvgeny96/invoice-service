package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

data class CreateOrderDataV2(
    val invoiceId: UUID,
    val url: String,
    val urlPayPageShort: String?,
)
