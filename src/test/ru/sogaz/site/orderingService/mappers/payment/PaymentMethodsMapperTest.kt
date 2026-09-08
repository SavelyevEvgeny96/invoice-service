package ru.sogaz.site.orderingService.mappers.payment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers
import ru.sogaz.site.orderingService.dto.response.PaymentDetailsDto
import ru.sogaz.site.orderingService.dto.response.PaymentMethodDto
import ru.sogaz.site.orderingService.dto.response.PaymentSystemDto
import java.util.UUID

class PaymentMethodsMapperTest {
    private val mapper = Mappers.getMapper(PaymentMethodsMapper::class.java)

    @Test
    fun `payment method maps to saved GID card`() {
        val cardId = UUID.randomUUID()
        val method =
            PaymentMethodDto(
                id = cardId,
                type = "card",
                supportsRecurring = false,
                acquirer = null,
                provider = null,
                paymentSystem = PaymentSystemDto("MIR", "MIR"),
                details = PaymentDetailsDto("Saved card", "0013"),
            )

        val result = mapper.toSavedCardGid(method)

        assertThat(result.keyCard).isEqualTo(cardId.toString())
        assertThat(result.lastDigits).isEqualTo("0013")
        assertThat(result.paymentSystem).isEqualTo("MIR")
    }
}
