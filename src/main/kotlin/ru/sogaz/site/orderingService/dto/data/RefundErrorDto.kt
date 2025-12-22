package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

data class RefundErrorDto(
    val metaInfo: MetaInfoOrder,
    val orderId: UUID,
    val status: String,
    val errorText: String,
)
