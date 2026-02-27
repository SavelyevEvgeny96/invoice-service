package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity

interface PaymentService {
    fun payCard(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage

    fun paySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage
}
