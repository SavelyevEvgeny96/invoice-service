package ru.sogaz.site.orderingService.controller.v2

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.v2.InvoicePayPageInfoV2Api
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.site.orderingService.service.order.OrderPaymentPageService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.util.UUID

@RestController
class InvoicePayPageInfoController(
    private val orderPaymentPageService: OrderPaymentPageService,
): InvoicePayPageInfoV2Api {
    companion object {
        private const val SUCCESS_STATUS_CODE_PAY_INFO_PAGE = 1101540200
        private const val SUCCESS_STATUS_CODE_INVOICE_META_INFO = 1101544200
    }
    override fun getInvoicePayPage(
        invoiceId: UUID,
        payQueryParams: PayQueryParams,
        saveCard: Boolean,
        unifiedId: String?
    ): Response<InvoicePayPageInfo> =
        orderPaymentPageService
            .getInvoicePayPageInfo(invoiceId, payQueryParams)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_PAY_INFO_PAGE)

    override fun getInvoiceMetaInfo(invoiceId: UUID): Response<InvoiceMetaInfo> =
        orderPaymentPageService
            .getMetaInfo(invoiceId)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_INVOICE_META_INFO)

    private fun <T> T.wrapToSuccessResponse(statusCode: Int): Response<T> = getSuccessResponse(getTraceId(), statusCode, this)
}