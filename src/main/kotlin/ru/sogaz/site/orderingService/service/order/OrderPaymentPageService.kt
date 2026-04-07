package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import java.util.UUID

interface OrderPaymentPageService {
    fun getPaymentPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo

    fun getMetaInfo(orderId: UUID): InvoiceMetaInfo
}
