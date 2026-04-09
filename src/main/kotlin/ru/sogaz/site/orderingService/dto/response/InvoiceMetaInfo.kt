package ru.sogaz.site.orderingService.dto.response

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
