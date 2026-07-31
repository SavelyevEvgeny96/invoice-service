package ru.sogaz.site.orderingService.validation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.dto.request.PayerFio
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.enums.PaymentQrBank

class OrderRequestV2QrValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `QR banking accepts provided payer FIO`() {
        val request =
            OrderRequestV2(
                paymentMethodList = listOf(PaymentMethod.QR_BANKING_DETAILS),
                bankQr = listOf(PaymentQrBank.GPB),
                payerFio = PayerFio("Иванов", "Иван", "Иванович"),
            )

        assertThat(validator.validateProperty(request, "payerFio")).isEmpty()
        assertThat(validator.validate(request).none { it.propertyPath.toString() == "payerFio" }).isTrue()
    }

    @Test
    fun `QR banking rejects missing payer FIO`() {
        val request = OrderRequestV2(paymentMethodList = listOf(PaymentMethod.QR_BANKING_DETAILS))

        assertThat(validator.validate(request).map { it.propertyPath.toString() }).contains("payerFio")
    }

    @Test
    fun `bankQR single string alias is deserialized as enum list`() {
        val request =
            jacksonObjectMapper().readValue(
                """
                {
                  "paymentMethodList":["QR_BANKING_DETAILS"],
                  "bankQR":"GPB",
                  "payerFio":{"lastName":"Иванов","firstName":"Иван","middleName":"Иванович"}
                }
                """.trimIndent(),
                OrderRequestV2::class.java,
            )

        assertThat(request.bankQr).containsExactly(PaymentQrBank.GPB)
        assertThat(request.payerFio?.lastName).isEqualTo("Иванов")
        assertThat(validator.validate(request).none { it.propertyPath.toString() == "payerFio" }).isTrue()
    }
}
