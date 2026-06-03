package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.apiDoc.PayApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.service.order.OrderService
import java.util.UUID

@RestController
class PayController(
    private val orderService: OrderService,
) : PayApi {
    override fun payCard(
        orderId: UUID,
        originalForwardedFor: String?,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .payCard(
                orderId = orderId,
                payQueryParams = payQueryParams.withPayerIpFromHeader(originalForwardedFor),
            ).wrapToRedirectView()

    override fun paySbp(
        orderId: UUID,
        originalForwardedFor: String?,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .paySbp(
                orderId = orderId,
                payQueryParams = payQueryParams.withPayerIpFromHeader(originalForwardedFor),
            ).wrapToRedirectView()

    private fun PayQueryParams.withPayerIpFromHeader(originalForwardedFor: String?): PayQueryParams =
        PayQueryParams(
            urlToReturn = urlToReturn,
            urlToReturnS = urlToReturnS,
            urlToReturnF = urlToReturnF,
            depersonalization = depersonalization,
            channelSale = channelSale,
            payerIP = originalForwardedFor.normalizeHeaderValue() ?: payerIP,
        )

    private fun String?.normalizeHeaderValue(): String? =
        this
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    private fun PaymentPage.wrapToRedirectView() = uri.run(::RedirectView)
}
