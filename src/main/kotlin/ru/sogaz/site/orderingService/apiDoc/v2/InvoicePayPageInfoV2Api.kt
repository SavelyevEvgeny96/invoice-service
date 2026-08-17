package ru.sogaz.site.orderingService.apiDoc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface InvoicePayPageInfoV2Api {
    companion object {
        const val X_REAL_IP = "x-real-ip"
    }

    @Operation(
        summary = "Информация о способах оплаты счета",
        description = "Возвращает ссылку для оплаты картой и, если возможно оплатить по СБП, QR-code для оплаты по СБП",
    )
    @GetMapping("/v2/invoice/pagepayinfo/{invoiceId}")
    fun getInvoicePayPage(
        @Parameter(
            name = X_REAL_IP,
            description = "IP пользователя из заголовка",
            `in` = ParameterIn.HEADER,
            required = false,
            schema = Schema(type = "string"),
        )
        @RequestHeader(name = X_REAL_IP, required = false)
        xRealIp: String?,
        @Parameter(
            name = "invoiceId",
            description = "UUID счета для оплаты",
            required = true,
            `in` = ParameterIn.PATH,
            schema = Schema(type = "string", format = "uuid"),
        )
        @PathVariable("invoiceId")
        invoiceId: UUID,
        @ParameterObject
        payQueryParams: PayQueryParams,
        @Parameter(
            name = "unifiedId",
            required = false,
            `in` = ParameterIn.QUERY,
            schema = Schema(type = "string"),
        )
        @RequestParam(required = false)
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
        @Parameter(
            name = "payment",
            description = "Признак необходимости вернуть информацию с учетом оплаты. По умолчанию false",
            required = false,
            `in` = ParameterIn.QUERY,
            schema = Schema(type = "boolean", defaultValue = "false"),
        ) payment: Boolean,
    ): Response<InvoiceMetaInfo>
}
