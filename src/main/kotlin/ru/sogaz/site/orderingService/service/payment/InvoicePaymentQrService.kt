package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.response.InvoicePaymentQr
import java.util.UUID

interface InvoicePaymentQrService {
    fun generate(
        invoiceId: UUID,
        bank: String,
    ): InvoicePaymentQr
}
