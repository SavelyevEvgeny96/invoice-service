package ru.sogaz.site.orderingService.mappers.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers
import ru.sogaz.site.orderingService.dto.request.OrderRequestV1
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.enums.PaymentQrBank

class OrderRequestCommandMapperTest {
    private val mapper = Mappers.getMapper(OrderRequestCommandMapper::class.java)

    @Test
    fun `v2 maps QR bank enum list to command storage string`() {
        val request = OrderRequestV2(bankQr = listOf(PaymentQrBank.GPB, PaymentQrBank.VTB))

        val command = mapper.toCommand(request)

        assertThat(command.bankQr).isEqualTo("GPB,VTB")
    }

    @Test
    fun `v1 preserves null QR banks`() {
        val command = mapper.toCommand(OrderRequestV1(bankQr = null))

        assertThat(command.bankQr).isNull()
    }
}
