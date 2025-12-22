package ru.sogaz.site.orderingService.dto.data

data class Parsed<T>(
    val tag: Long,
    val dto: T,
    val messageId: String?,
)
