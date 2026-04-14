package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.service.payment.PayInfoService
import ru.sogaz.site.orderingService.service.payment.PaymentMethodURIBuilder
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.payment.QrGeneratorService
import java.net.URI

@Service
class PayInfoServiceImpl(
    private val paymentService: PaymentService,
    private val paymentMethodURIBuilder: PaymentMethodURIBuilder,
    private val qrGeneratorService: QrGeneratorService,
    @param:Value("\${api.payment.isSbpActive}")
    private val isSbpActive: Boolean,
    @param:Value("\${api.payment.isQrGeneratorActive}")
    private val isQrGeneratorActive: Boolean,
    @param:Value("\${api.payment.qrCodeSize}")
    private val qrCodeSize: Int,
) : PayInfoService {
    companion object {
        private const val NULL_ORDER_ID_ERROR = "orderId не может быть пустым"
    }

    override fun getInfo(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): Pair<URI, PaySbp?> {
        val payCardLink = order.formPayCardLink(payQueryParams)
        val paySbp =
            when {
                isSbpActive && isQrGeneratorActive -> formInnerQrPaySbp(order, payQueryParams)
                isSbpActive -> getBankQrPaySbp(order, payQueryParams)
                else -> null
            }
        return Pair(payCardLink, paySbp)
    }

    private fun formInnerQrPaySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaySbp? =
        order
            .formPaySbpLink(payQueryParams)
            .run(::generatePaySbpByUri)

    private fun generatePaySbpByUri(paySbpLink: URI) =
        qrGeneratorService
            .generateFileQR(paySbpLink, qrCodeSize)
            ?.let { PaySbp(paySbpLink.toString(), it) }

    private fun getBankQrPaySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaySbp? = paymentService.payQrSbp(order, payQueryParams)

    private fun OrderEntity.formPayCardLink(payQueryParams: PayQueryParams): URI =
        paymentMethodURIBuilder.buildPayCardURI(requireNotNull(orderId) { NULL_ORDER_ID_ERROR }, payQueryParams)

    private fun OrderEntity.formPaySbpLink(payQueryParams: PayQueryParams): URI =
        paymentMethodURIBuilder.buildPaySbpURI(requireNotNull(orderId) { NULL_ORDER_ID_ERROR }, payQueryParams)
}
