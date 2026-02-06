package ru.sogaz.site.orderingService.dto.request

import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import java.math.BigDecimal

data class SellRefundMessageDto(
    val metaInfo: List<MetaInfoOrder>,
    val type: String,
    val client: Client,
    val items: List<Item>,
    val paymentMethod: String,
    val paymentObject: String,
    val vat: Vat,
    val payments: List<Payment>,
    val total: BigDecimal,
    val system: String,
    val version: String,
    val depersonalization: Boolean,
    val channel: String,
    val product: String,
    val orderId: Long,
) {
    data class Client(
        val email: String?,
        val phone: String?,
        val name: String?,
        val userId: Long?,
        val unifiedId: String?,
    )

    data class Item(
        val name: String,
        val price: BigDecimal?,
        val quantity: BigDecimal,
        val sum: BigDecimal?,
    )

    data class Vat(
        val type: String,
    )

    data class Payment(
        val type: String,
        val sum: BigDecimal?,
    )
}
