package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.InvoicePayCardGidRequest
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import java.util.UUID

interface InvoicePayCardGidService {
    fun createPayment(
        invoiceId: UUID,
        request: InvoicePayCardGidRequest,
    ): PaymentPage
}
