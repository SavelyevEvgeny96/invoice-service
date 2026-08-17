package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.net.URI
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoicePayPageInfo(
    val invoiceId: UUID,
    val premiumAmount: String? = null,
    val accounts: List<InvoiceAccountData> = emptyList(),
    val urlPayBank: URI? = null,
    val paySbp: PaySbp? = null,
    val qrBankingDetails: QrBankingDetails? = null,
    val gidId: String? = null,
    val gidPayUrl: String? = null,
    val listSavedCardsGid: List<SavedCardGid>? = emptyList(),
)

data class InvoiceAccountData(
    val policyNumber: String?,
    val agreementPrice: BigDecimal?,
    val agreementNumber: String?,
    val typeOperation: String?,
    val insuranceKind: String?,
)

data class SavedCardGid(
    val keyCard: String?,
    val lastDigits: String?,
    val paymentSystem: String?,
)
