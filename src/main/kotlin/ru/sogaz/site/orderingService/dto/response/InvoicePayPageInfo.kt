package ru.sogaz.site.orderingService.dto.response

import java.math.BigDecimal
import java.net.URI
import java.util.UUID

data class InvoicePayPageInfo(
    val invoiceId: UUID,
    val premiumAmount: String? = null,
    val accounts: List<InvoiceAccountData> = emptyList(),
    val urlPayBank: URI,
    val paySbp: PaySbp? = null,
)

data class InvoiceAccountData(
    val policyNumber: String?,
    val agreementPrice: BigDecimal?,
    val agreementNumber: String?,
    val typeOperation: String?,
    val insuranceKind: String?,
)
