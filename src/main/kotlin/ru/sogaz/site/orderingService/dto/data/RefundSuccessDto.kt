package ru.sogaz.site.orderingService.dto.data

import java.util.*

data class RefundSuccessDto (
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID,
    val bank: String?,
)