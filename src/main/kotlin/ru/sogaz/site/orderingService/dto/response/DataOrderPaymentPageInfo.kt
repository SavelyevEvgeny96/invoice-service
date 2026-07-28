package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataOrderPaymentPageInfo(
    val orderId: UUID,
    val premiumAmount: String? = null,
    val accounts: List<SubOrderInfo> = emptyList(),
    val urlPayBank: URI? = null,
    val paySbp: PaySbp? = null,
    val qrBankingDetails: QrBankingDetails? = null,
)

data class SubOrderInfo(
    val policyNumber: String?,
    val contractNumber: String?,
    val typeInsurance: String?,
    val insuranceProgram: String?,
)

data class PaySbp(
    val urlPay: String,
    val fileQR: FileQR,
)

data class FileQR(
    val content: String,
    val mediaType: String,
)
