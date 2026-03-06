package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.apiDoc.PayApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.service.OrderService
import java.util.UUID

@RestController
class PayController(
    private val orderService: OrderService,
) : PayApi {
    override fun payCard(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .payCard(orderId, payQueryParams)
            .wrapToRedirectView()

    override fun paySbp(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .paySbp(orderId, payQueryParams)
            .wrapToRedirectView()

    private fun PaymentPage.wrapToRedirectView() = uri.toString().run(::RedirectView)
}
