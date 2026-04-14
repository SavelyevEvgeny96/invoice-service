package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceStatusEvent(
    val invoiceId: String?,
    val externalSystemCode: String? = null,
    val externalId: String?,
    val status: String?,
    val email: String?,
    val errorText: String? = null,
    val invoices: List<SubInvoiceData>?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubInvoiceData(
    val premium: String?,
    val policyId: String?,
    val policyNumber: String?,
    val policyDate: String?,
    val contractId: String?,
    val contractNumber: String?,
    val contractDate: String?,
    val typeInsurance: String?,
    val insuranceKind: String?,
    val channel: String?,
)
