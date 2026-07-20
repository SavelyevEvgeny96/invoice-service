package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import java.util.UUID

interface OrderPaymentPageService {
    fun getInvoicePayPageInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): InvoicePayPageInfo

    fun getPaymentPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo

    fun getMetaInfo(
        orderId: UUID,
        payment: Boolean,
    ): InvoiceMetaInfo
}
