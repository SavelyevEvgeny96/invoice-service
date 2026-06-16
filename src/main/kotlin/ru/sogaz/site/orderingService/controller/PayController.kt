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
        xRealIp: String?,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .payCard(
                orderId = orderId,
                payQueryParams = enrichParams(payQueryParams, originalForwardedFor, xRealIp),
            ).wrapToRedirectView()

    override fun paySbp(
        orderId: UUID,
        originalForwardedFor: String?,
        xRealIp: String?,
        payQueryParams: PayQueryParams,
    ): RedirectView =
        orderService
            .paySbp(
                orderId = orderId,
                payQueryParams = enrichParams(payQueryParams, originalForwardedFor, xRealIp),
            ).wrapToRedirectView()

    private fun buildPayerIp(
        originalForwardedForIp: String?,
        xRealIp: String?,
    ): String? {
        fun extractIps(raw: String?): List<String> =
            raw
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

        val forwardedIps = extractIps(originalForwardedForIp)
        val realIps = extractIps(xRealIp)

        val uniqueIps = (forwardedIps + realIps).distinct()

        return uniqueIps.takeIf { it.isNotEmpty() }?.joinToString(",")
    }

    private fun enrichParams(
        params: PayQueryParams,
        originalForwardedFor: String?,
        xRealIp: String?,
    ): PayQueryParams =
        PayQueryParams(
            urlToReturn = params.urlToReturn,
            urlToReturnS = params.urlToReturnS,
            urlToReturnF = params.urlToReturnF,
            depersonalization = params.depersonalization,
            channelSale = params.channelSale,
            payerIP = buildPayerIp(params.payerIP, "$xRealIp,$originalForwardedFor"),
        )

    private fun PaymentPage.wrapToRedirectView() = uri.run(::RedirectView)
}
