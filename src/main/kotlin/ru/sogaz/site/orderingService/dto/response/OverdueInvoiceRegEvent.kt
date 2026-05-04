package ru.sogaz.site.orderingService.dto.response

import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder

data class OverdueInvoiceRegEvent(
    val metaInfo: List<MetaInfoOrder>,
    val channel: String,
    val unifiedId: String,
    val status: String,
    val errorText: String,
)
