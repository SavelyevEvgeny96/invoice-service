package ru.sogaz.site.orderingService.validation

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.dto.request.PayerFio
import ru.sogaz.site.orderingService.dto.request.SubOrderRequestV2
import ru.sogaz.site.orderingService.enums.PaymentMethod
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class OrderRequestV2ValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `should accept payment method list`() {
        val errors = validator.validate(validRequest(paymentMethodList = listOf(PaymentMethod.CARD, PaymentMethod.SBP)))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `should accept qr banking details with payer fio`() {
        val errors =
            validator.validate(
                validRequest(
                    paymentMethodList = listOf(PaymentMethod.QR_BANKING_DETAILS),
                    payerFio = PayerFio("Иванов", "Иван", "Иванович"),
                ),
            )
        assertThat(errors).isEmpty()
    }

    @Test
    fun `should reject qr banking details without payer fio`() {
        val errors = validator.validate(validRequest(paymentMethodList = listOf(PaymentMethod.QR_BANKING_DETAILS)))
        assertThat(errors.map { it.messageTemplate }).contains("{validation.orderRequest.payerFio.required}")
    }

    @Test
    fun `should accept life insurance kind`() {
        val errors = validator.validate(validRequest(insuranceKind = "LIFE"))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `should reject invalid return url`() {
        val errors = validator.validate(validRequest(urlToReturn = "ftp://evil.example.com/payment"))
        assertThat(errors.map { it.propertyPath.toString() }).contains("urlToReturn")
    }

    @Test
    fun `should accept request without new optional fields`() {
        val errors = validator.validate(validRequest(paymentMethodList = null, payerFio = null, checkUrlReturn = null))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `should reject empty payment method list`() {
        val errors = validator.validate(validRequest(paymentMethodList = emptyList()))
        assertThat(errors.map { it.messageTemplate }).contains("{validation.orderRequest.paymentMethodList.notEmpty}")
    }

    @Test
    fun `should accept optional QR payment bank`() {
        val errors = validator.validate(validRequest(bankQr = "GPB"))
        assertThat(errors).isEmpty()
    }

    private fun validRequest(
        paymentMethodList: List<PaymentMethod>? = listOf(PaymentMethod.CARD),
        payerFio: PayerFio? = null,
        checkUrlReturn: Boolean? = true,
        bankQr: String? = null,
        insuranceKind: String = "OSAGO",
        urlToReturn: String? = "https://www.sogaz.ru/success",
    ) = OrderRequestV2(
        invoices =
            mutableListOf(
                SubOrderRequestV2(
                    premium = BigDecimal("100.00"),
                    agreementNumber = "A-1",
                    agreementId = "ID-1",
                    insuranceKind = insuranceKind,
                ),
            ),
        email = "client@sogaz.ru",
        saveCard = false,
        invoiceEndDate = Instant.now().plus(1, ChronoUnit.DAYS),
        urlToReturn = urlToReturn,
        urlToDecline = "https://www.sogaz.ru/decline",
        typePaymentOperation = "PAYMENT",
        paymentMethodList = paymentMethodList,
        payerFio = payerFio,
        checkUrlReturn = checkUrlReturn,
        bankQr = bankQr,
    )
}
