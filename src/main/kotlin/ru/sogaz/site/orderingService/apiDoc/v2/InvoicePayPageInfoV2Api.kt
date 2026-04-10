package ru.sogaz.site.orderingService.apiDoc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface InvoicePayPageInfoV2Api {
    @Operation(
        summary = "Информация о способах оплаты счета",
        description = "Возвращает ссылку для оплаты картой и, если возможно оплатить по СБП, QR-code для оплаты по СБП",
    )
    @Parameter(
        name = "invoiceId",
        description = "UUID счета для оплаты",
        required = true,
        schema = Schema(type = "string", format = "uuid"),
    )
    @Parameter(name = "saveCard", schema = Schema(type = "boolean", defaultValue = "false"))
    @GetMapping("v2/invoice/pagepayinfo/{invoiceId}")
    fun getInvoicePayPage(
        @PathVariable invoiceId: UUID,
        payQueryParams: PayQueryParams,
        saveCard: Boolean = false,
        unifiedId: String?,
    ): Response<InvoicePayPageInfo>

    @Operation(summary = "Информация о счете")
    @Parameter(
        name = "invoiceId",
        description = "UUID счета для оплаты",
        required = true,
        schema = Schema(type = "string", format = "uuid"),
    )
    @GetMapping("v2/invoice/metainfo/{invoiceId}")
    fun getInvoiceMetaInfo(
        @PathVariable invoiceId: UUID,
    ): Response<InvoiceMetaInfo>
}
