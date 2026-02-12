package ru.sogaz.site.orderingService.dto.data

sealed class ParsedResult<out T> {
    data class Success<T>(
        val tag: Long,
        val dto: T,
        val messageId: String?,
    ) : ParsedResult<T>()

    data class Error(
        val tag: Long,
        val rawMessage: String,
        val author: String,
        val messageId: String?,
    ) : ParsedResult<Nothing>()
}
