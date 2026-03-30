package ru.sogaz.site.orderingService.dto.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaidOrderMessage(
    val orderId: String?,
    val recipientEmail: String?,
    val externalSystemCode: String? = null,
    val subscriptionId: String?,
    val paySuccess: String?,
    val subOrders: List<SubOrderPayload>,
    val issuerName: String?,
    val paymentType: String?,
    val maskedPan: String?,
    val paymentSystem: String?,
    val status: String?,
    val keyCard: String?,
    var bank: String?,
    val errorText: String? = null,
) : Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubOrderPayload(
    val docType: String?,
    val policyId: String?,
    val policyNumber: String?,
    val contractNumber: String?,
    val contractId: String?,
    val typeInsurance: String?,
    val premiumAmount: String?,
    val channel: String?,
    val policyDate: Long?,
    val contractDate: Long?,
) : Serializable
