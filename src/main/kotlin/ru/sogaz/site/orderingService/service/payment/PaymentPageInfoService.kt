package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.entity.OrderEntity

interface PaymentPageInfoService {
    fun getInfo(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo
}
