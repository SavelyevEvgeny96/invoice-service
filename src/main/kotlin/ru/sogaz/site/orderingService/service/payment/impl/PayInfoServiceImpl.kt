package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.apiDoc.InvoicePayCardGidApi
import ru.sogaz.site.orderingService.apiDoc.integretion.LKApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.service.payment.PayInfoService
import ru.sogaz.site.orderingService.service.payment.PaymentMethodURIBuilder
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.payment.QrGeneratorService
import java.net.URI
import java.util.UUID

@Service
class PayInfoServiceImpl(
    private val invoicePayCardGidApi: InvoicePayCardGidApi,
    private val paymentService: PaymentService,
    private val paymentMethodURIBuilder: PaymentMethodURIBuilder,
    private val qrGeneratorService: QrGeneratorService,
    private val lkApi: LKApi,
    @param:Value("\${api.payment.qrCodeSize}")
    private val qrCodeSize: Int,
) : PayInfoService {
    companion object {
        private const val NULL_ORDER_ID_ERROR = "orderId не может быть пустым"
    }

    override fun getInfo(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
        paymentMethods: Set<PaymentMethod>,
    ): Pair<URI?, PaySbp?> {
        val payCardLink = if (PaymentMethod.CARD in paymentMethods) order.formPayCardLink(payQueryParams) else null
        // Проверяем, есть ли СБП ИЛИ Цифровой рубль
        val hasQrPayment = paymentMethods.any { it == PaymentMethod.SBP || it == PaymentMethod.DIGITAL_RUB }
        val paySbp = if (hasQrPayment) formPaySbp(order, payQueryParams) else null
        return Pair(payCardLink, paySbp)
    }

    override fun getGidIdInfo(payQueryParams: PayQueryParams, invoiceId: UUID): Pair<GidAuthResponse, String> {

        val gidAuthResponse = lkApi.getGidId(payQueryParams.gidId)

        val gitUrlResponse = invoicePayCardGidApi.payCardGid()

    }

    private fun formPaySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaySbp? =
        runCatching { paymentService.paySbp(order, payQueryParams) }
            .getOrNull()
            ?.uri
            ?.let { paymentPageUrl -> generatePaySbpByUri(URI.create(paymentPageUrl)) }

    private fun generatePaySbpByUri(paySbpLink: URI) =
        qrGeneratorService
            .generateFileQR(paySbpLink, qrCodeSize)
            ?.let { PaySbp(paySbpLink.toString(), it) }

    private fun OrderEntity.formPayCardLink(payQueryParams: PayQueryParams): URI =
        paymentMethodURIBuilder.buildPayCardURI(requireNotNull(orderId) { NULL_ORDER_ID_ERROR }, payQueryParams)
}
