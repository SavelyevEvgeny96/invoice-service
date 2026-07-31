package ru.sogaz.site.orderingService.mappers.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers
import ru.sogaz.site.orderingService.dto.response.QrBankingDetails
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.util.UUID

class PaymentMethodsMapperTest {
    private val mapper = Mappers.getMapper(PaymentMethodsMapper::class.java)

    @Test
    fun `invoice payment info contains QR banking availability`() {
        val orderId = UUID.randomUUID()
        val order = OrderEntity(orderId = orderId, queueStatusResultName = "queue")
        val details = QrBankingDetails(gpbAvailability = true, vtpAvailability = false)

        val result = mapper.toInvoicePayPageInfo(order, null, null, details)

        assertThat(result.invoiceId).isEqualTo(orderId)
        assertThat(result.qrBankingDetails).isEqualTo(details)
    }

    @Test
    fun `order payment info serializes vtpAvailability with specification name`() {
        val order = OrderEntity(orderId = UUID.randomUUID(), queueStatusResultName = "queue")
        val details = QrBankingDetails(gpbAvailability = false, vtpAvailability = true)

        val result = mapper.toDataOrderPaymentPageInfo(order, null, null, details)
        val json = jacksonObjectMapper().writeValueAsString(result)

        assertThat(json).contains("\"qrBankingDetails\"")
        assertThat(json).contains("\"vtpAvailability\":true")
    }
}
