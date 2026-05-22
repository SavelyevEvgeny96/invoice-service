package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

/**
 * Ответ о результате обработки запроса на возврат.
 */
data class RefundResponseDto(
    val invoiceId: UUID,
    val status: String,
    val errorText: String,
)
