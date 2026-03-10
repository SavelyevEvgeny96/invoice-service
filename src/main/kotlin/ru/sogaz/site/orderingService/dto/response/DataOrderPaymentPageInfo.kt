package ru.sogaz.site.orderingService.dto.response

import ru.sogaz.site.orderingService.enums.MediaTypeValue
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
    val urlPay: URI,
    val fileQR: FileQR,
) {
    constructor(stringUrl: String, fileQR: FileQR) : this(URI.create(stringUrl), fileQR)
}

data class FileQR(
    val content: String,
    val mediaType: String,
) {
    constructor(content: String, mediaType: MediaTypeValue) : this(content, mediaType.value)
}
