package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentMethod
import java.net.URI
import java.util.UUID

interface PayInfoService {
    fun getInfo(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
        paymentMethods: Set<PaymentMethod>,
    ): Pair<URI?, PaySbp?>

    fun getGidIdInfo(
        payQueryParams: PayQueryParams,
        invoiceId: UUID,
    ): Pair<GidAuthResponse?, String?>
}
