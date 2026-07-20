package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.PaymentQrBank
import ru.sogaz.site.orderingService.validation.constraint.ValidEnum
import java.util.UUID

data class InvoicePaymentQrRequest(
    @field:NotNull(message = "{validation.invoicePaymentQr.invoiceId.notNull}")
    val invoiceId: UUID?,
    @field:NotBlank(message = "{validation.invoicePaymentQr.bank.notBlank}")
    @field:ValidEnum(
        enumClass = PaymentQrBank::class,
        message = "{validation.invoicePaymentQr.bank.invalid}",
    )
    val bank: String?,
)
