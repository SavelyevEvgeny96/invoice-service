package ru.sogaz.site.orderingService.dto.data

data class ParsedData<T>(
    val tag: Long,
    val dto: T,
    val messageId: String?,
)
