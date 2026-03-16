package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface PaymentPageInfoApi {
    @Operation(
        summary = "Информация о способах оплаты заказа",
        description = "Возвращает ссылку для оплаты картой и, если возможно оплатить по СБП, QR-code для оплаты по СБП",
    )
    @Parameter(name = "orderId", description = "UUID заказа для оплаты", required = true, schema = Schema(type = "uuid"))
    @GetMapping("/order/pagepayinfo/{orderId}")
    fun getInfoPage(
        @PathVariable orderId: UUID,
        payQueryParams: PayQueryParams,
    ): Response<DataOrderPaymentPageInfo>
}
