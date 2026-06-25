package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class InvoiceMetaInfo(
    val invoiceId: UUID,
    val invoiceStatus: String,
    val premiumAmount: BigDecimal?,
    val accounts: List<InvoiceMetaAccount>?,
    val invoiceEndDate: Instant?,
    val externalId: String?,
    val typePaymentOperation: String?,
    val email: String?,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val payment: InvoiceMetaPayment? = null,
)

data class InvoiceMetaPayment(
    val paymentId: UUID?,
    val status: String,
    val bank: String?,
    val operation: String?,
    val type: String?,
    val amount: BigDecimal?,
    val depersonalization: Boolean?,
    val paymentBankId: String?,
    val pan: String?,
    val paymentSystem: String?,
    val payDate: Instant?,
    val payerIp: String?,
    val externalErrorCode: String?,
    val errorText: String?,
)

data class InvoiceMetaAccount(
    val policyNumber: String?,
    val policyDate: Instant?,
    val agreementPrice: String?,
    val agreementNumber: String?,
    val agreementDate: Instant?,
    val typeOperation: String?,
    val insuranceKind: String?,
    val program: String?,
    val channel: String?,
)
