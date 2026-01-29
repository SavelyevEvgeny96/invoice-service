package ru.sogaz.site.orderingService.dto.data

data class RefundPreparationResult(
    val found: List<RefundPayloadDto>,
    val missing: List<RefundPayloadDto>,
    val noAccess: List<RefundPayloadDto>,
    val notForPaid: List<RefundPayloadDto>,
)
