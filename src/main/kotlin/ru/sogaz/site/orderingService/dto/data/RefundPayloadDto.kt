package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

/**
 * Входящее сообщение для запуска сценария возврата.
 */
data class RefundPayloadDto(
    val invoiceId: UUID? = null,
)
