package ru.sogaz.site.orderingService.dto.data

import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto

data class RefundPreparationResult(
    val found: List<ParsedData<RefundPayloadDto>>,
    val missing: List<ParsedData<RefundPayloadDto>>,
    val noAccess: List<ParsedData<RefundPayloadDto>>,
    val notForPaid: List<ParsedData<RefundPayloadDto>>,
)
