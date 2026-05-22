package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OverdueInvoiceV2Event(
    val invoiceId: String?,
    val externalSystemCode: String?,
    val externalId: String?,
    val status: String?,
    val errorText: String?,
    val invoices: List<OverdueSubInvoiceV2Dto>?,
    val email: String?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OverdueSubInvoiceV2Dto(
    val premium: String?,
    val policyId: String?,
    val policyNumber: String?,
    val policyDate: String?,
    val agreementId: String?,
    val agreementNumber: String?,
    val agreementDate: String?,
    val insuranceKind: String?,
    val channel: String?,
)
