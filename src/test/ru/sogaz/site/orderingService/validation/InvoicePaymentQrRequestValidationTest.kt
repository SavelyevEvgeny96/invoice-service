package ru.sogaz.site.orderingService.validation

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.dto.request.InvoicePaymentQrRequest
import java.util.UUID

class InvoicePaymentQrRequestValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `should accept supported banks`() {
        listOf("GPB", "VTB").forEach { bank ->
            assertThat(validator.validate(InvoicePaymentQrRequest(UUID.randomUUID(), bank))).isEmpty()
        }
    }

    @Test
    fun `should reject blank and unsupported bank`() {
        val blankBankErrors = validator.validate(InvoicePaymentQrRequest(UUID.randomUUID(), ""))
        val unsupportedBankErrors = validator.validate(InvoicePaymentQrRequest(UUID.randomUUID(), "SBER"))

        assertThat(blankBankErrors.map { it.messageTemplate })
            .contains("{validation.invoicePaymentQr.bank.notBlank}", "{validation.invoicePaymentQr.bank.invalid}")
        assertThat(unsupportedBankErrors.map { it.messageTemplate })
            .containsExactly("{validation.invoicePaymentQr.bank.invalid}")
    }

    @Test
    fun `should reject missing invoice id`() {
        val errors = validator.validate(InvoicePaymentQrRequest(null, "GPB"))

        assertThat(errors.single().messageTemplate).isEqualTo("{validation.invoicePaymentQr.invoiceId.notNull}")
    }
}
