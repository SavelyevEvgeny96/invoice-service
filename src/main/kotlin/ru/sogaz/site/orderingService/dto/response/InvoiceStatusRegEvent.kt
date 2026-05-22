package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceStatusRegEvent(
    val channel: String,
    val unifiedId: String,
    val status: String,
    val errorText: String? = null,
    val keyCard: String? = null,
    val maskedPan: String? = null,
    val title: String? = null,
    val paymentSystem: String? = null,
    val issuerName: String? = null,
    val paymentType: String? = null,
    val bank: String? = null,
    val payDate: String? = null,
)
