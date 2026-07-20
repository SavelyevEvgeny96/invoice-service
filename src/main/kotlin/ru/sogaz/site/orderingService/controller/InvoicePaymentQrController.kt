package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.InvoicePaymentQrApi
import ru.sogaz.site.orderingService.dto.request.InvoicePaymentQrRequest
import ru.sogaz.site.orderingService.dto.response.InvoicePaymentQr
import ru.sogaz.site.orderingService.service.payment.InvoicePaymentQrService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

@RestController
class InvoicePaymentQrController(
    private val invoicePaymentQrService: InvoicePaymentQrService,
) : InvoicePaymentQrApi {
    companion object {
        private const val SUCCESS_STATUS_CODE = 1101534200
    }

    override fun getPaymentQr(request: InvoicePaymentQrRequest): Response<InvoicePaymentQr> =
        getSuccessResponse(
            getTraceId(),
            SUCCESS_STATUS_CODE,
            invoicePaymentQrService.generate(checkNotNull(request.invoiceId), checkNotNull(request.bank)),
        )
}
