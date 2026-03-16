package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import java.net.URI
import java.util.UUID

interface PaymentMethodURIBuilder {
    fun buildPayCardURI(
        orderId: UUID,
        payQueryParams: PayQueryParams = PayQueryParams(),
    ): URI

    fun buildPaySbpURI(
        orderId: UUID,
        payQueryParams: PayQueryParams = PayQueryParams(),
    ): URI
}
