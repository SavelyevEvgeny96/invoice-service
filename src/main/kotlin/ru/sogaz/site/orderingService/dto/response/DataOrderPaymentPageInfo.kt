package ru.sogaz.site.orderingService.dto.response

import java.net.URI
import java.util.UUID

data class DataOrderPaymentPageInfo(
    val orderId: UUID,
    val premiumAmount: String? = null,
    val accounts: List<SubOrderInfo> = emptyList(),
    val urlPayBank: URI,
    val paySbp: PaySbp? = null,
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
