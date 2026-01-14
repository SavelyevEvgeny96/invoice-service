package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import java.util.UUID

interface PayApi {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
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
            schema = Schema(type = "boolean"),
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @GetMapping("orders/payCard/{orderId}")
    fun payCard(
        @PathVariable orderId: UUID,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
    ): RedirectView
}
