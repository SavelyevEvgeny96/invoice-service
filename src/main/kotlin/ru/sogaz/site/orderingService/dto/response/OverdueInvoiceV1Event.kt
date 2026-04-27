package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OverdueInvoiceV1Event(
    val orderId: String?,
    val externalSystemCode: String?,
    val subscriptionId: String?,
    val status: String?,
    val errorText: String?,
    val subOrders: List<OverdueSubOrderV1Dto>?,
    val recipientEmail: String?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OverdueSubOrderV1Dto(
    val docType: String?,
    val premiumAmount: String?,
    val policyId: String?,
    val policyNumber: String?,
    val policyDate: String?,
    val contractNumber: String?,
    val contractId: String?,
    val contractDate: String?,
    val typeInsurance: String?,
    val insuranceProgram: String?,
    val channel: String?,
)
