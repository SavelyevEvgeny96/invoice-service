package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.orderingService.dto.response.CompletedPaymentInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface OrderInfoApi {
    @Operation(
        summary = "Информация о совершенных по заказу платежах",
    )
    @Parameter(name = "orderId", description = "UUID заказа", required = true, schema = Schema(type = "string", format = "uuid"))
    @GetMapping("v1/order/info/{orderId}")
    fun getInfoPage(
        @PathVariable orderId: UUID,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): Response<CompletedPaymentInfo?>
}
