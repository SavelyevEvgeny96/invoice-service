package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
class InvoiceReversalStatusEvent(
    val invoiceId: String,
    val premium: BigDecimal,
    val status: String,
    val errorText: String? = null,
)
