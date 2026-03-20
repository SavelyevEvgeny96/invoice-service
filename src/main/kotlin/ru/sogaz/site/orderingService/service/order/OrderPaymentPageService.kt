package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import java.util.UUID

interface OrderPaymentPageService {
    fun getInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo
}
