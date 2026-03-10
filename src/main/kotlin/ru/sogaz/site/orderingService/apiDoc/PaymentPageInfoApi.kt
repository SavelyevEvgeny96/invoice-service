package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
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
    @Parameters(
        Parameter(name = "orderId", description = "UUID заказа для оплаты", required = true, schema = Schema(type = "string")),
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            example = "http://www.sogaz.ru",
            schema = Schema(type = "string"),
        ),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты", schema = Schema(type = "string")),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты", schema = Schema(type = "string")),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            example = "true",
            schema = Schema(type = "string"),
        ),
    )
    @GetMapping("/order/pagepayinfo/{orderId}")
    fun getInfoPage(
        @PathVariable orderId: UUID,
        payQueryParams: PayQueryParams,
    ): Response<DataOrderPaymentPageInfo>
}