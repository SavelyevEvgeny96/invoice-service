package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.PaymentPageInfoApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.service.order.OrderPaymentPageService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.util.UUID

@RestController
class PaymentPageInfoController(
    private val orderPaymentPageService: OrderPaymentPageService,
) : PaymentPageInfoApi {
    companion object {
        private const val SUCCESS_STATUS_CODE_PAY_INFO_PAGE = 1101540200
        private const val SUCCESS_STATUS_CODE_INVOICE_META_INFO = 1101544200
    }

    override fun getInfoPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
        saveCard: Boolean,
        unifiedId: String?,
    ): Response<DataOrderPaymentPageInfo> =
        orderPaymentPageService.getPaymentPage(orderId, payQueryParams)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_PAY_INFO_PAGE)

    override fun getMetaInfo(invoiceId: UUID): Response<InvoiceMetaInfo> =
        orderPaymentPageService.getMetaInfo(invoiceId)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_INVOICE_META_INFO)

    private fun <T> T.wrapToSuccessResponse(statusCode: Int): Response<T> = getSuccessResponse(getTraceId(), statusCode, this)
}
