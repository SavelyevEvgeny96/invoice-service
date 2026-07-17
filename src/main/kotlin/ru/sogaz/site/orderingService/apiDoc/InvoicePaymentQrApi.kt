package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.sogaz.site.orderingService.dto.request.InvoicePaymentQrRequest
import ru.sogaz.site.orderingService.dto.response.InvoicePaymentQr
import ru.sogaz.siter.models.resonses.Response

@RequestMapping("/v1/invoice")
interface InvoicePaymentQrApi {
    @Operation(
        summary = "Получить QR для оплаты по реквизитам",
        description = "Формирует платежные реквизиты по ГОСТ Р 56042-2014 и возвращает QR-код.",
    )
    @ApiResponse(responseCode = "200", description = "QR-код успешно сформирован")
    @ApiResponse(responseCode = "409", description = "Заказ не найден, оплачен или недоступен для оплаты")
    @PostMapping("/payqr", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPaymentQr(
        @Valid @RequestBody request: InvoicePaymentQrRequest,
    ): Response<InvoicePaymentQr>
}
