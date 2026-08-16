package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.apiDoc.InvoicePayCardGidApi
import ru.sogaz.site.orderingService.dto.request.InvoicePayCardGidRequest
import ru.sogaz.site.orderingService.service.payment.InvoicePayCardGidService
import java.util.UUID

@RestController
class InvoicePayCardGidController(
    private val invoicePayCardGidService: InvoicePayCardGidService,
) : InvoicePayCardGidApi {
    override fun payCardGid(
        invoiceId: UUID,
        request: InvoicePayCardGidRequest,
    ): RedirectView = RedirectView(invoicePayCardGidService.createPayment(invoiceId, request).uri)
}
