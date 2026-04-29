package ru.sogaz.site.orderingService.dto.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RefundResponseDto(
    val metaInfo: List<MetaInfoOrder>,
    val invoiceId: UUID,
    val status: String,
    val amount: BigDecimal?,
    val errorText: String? = null,
)
