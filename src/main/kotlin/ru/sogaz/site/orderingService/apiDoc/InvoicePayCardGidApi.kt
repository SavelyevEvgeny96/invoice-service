package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.dto.request.InvoicePayCardGidRequest
import java.util.UUID

interface InvoicePayCardGidApi {
    @Operation(
        summary = "Оплата счета банковской картой через ГИД",
        description = "Проверяет счет, регистрирует платеж в payment-service и перенаправляет клиента на страницу банка",
    )
    @ApiResponse(responseCode = "200", description = "Перенаправление на страницу оплаты")
    @ApiResponse(responseCode = "409", description = "Счет не найден, уже оплачен или недоступен для оплаты")
    @ApiResponse(responseCode = "504", description = "Платежная система недоступна")
    @PostMapping("v1/invoice/paycardgid/{invoiceId}")
    fun payCardGid(
        @PathVariable invoiceId: UUID,
        @Valid @RequestBody request: InvoicePayCardGidRequest,
    ): RedirectView
}
