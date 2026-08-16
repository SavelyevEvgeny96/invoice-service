package ru.sogaz.site.orderingService.service.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.dao.CompanyDetailsQrDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.response.FileQR
import ru.sogaz.site.orderingService.entity.CompanyDetailsQrEntity
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.mappers.payment.PaymentQrMapperImpl
import ru.sogaz.site.orderingService.service.payment.impl.InvoicePaymentQrServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class InvoicePaymentQrServiceTest {
    private val orderDao = mockk<OrderDao>()
    private val companyDetailsQrDao = mockk<CompanyDetailsQrDao>()
    private val qrGeneratorService = mockk<QrGeneratorService>()
    private val service =
        InvoicePaymentQrServiceImpl(orderDao, companyDetailsQrDao, PaymentQrMapperImpl(), qrGeneratorService)

    @Test
    fun `should build gost payment string using first sub order and return qr`() {
        val invoiceId = UUID.randomUUID()
        val order =
            OrderEntity(
                orderId = invoiceId,
                payerLastName = "Иванов",
                payerFirstName = "Иван",
                payerMiddleName = "Иванович",
                premiumAmount = BigDecimal("154.52"),
                recipientEmail = "",
                recipientPhone = "",
                queueStatusResultName = "status",
            )
        order.addSubOrder(subOrder("FIRST", "2026-01-01T00:00:00Z"))
        order.addSubOrder(subOrder("SECOND", "2026-02-01T00:00:00Z"))
        val company = company()
        every { orderDao.findById(invoiceId) } returns order
        every { orderDao.findByIdWithoutLock(invoiceId) } returns order
        every { companyDetailsQrDao.findByBank("GPB") } returns company
        every { qrGeneratorService.generateFileQR(any<String>()) } returns FileQR("base64", "image/png")

        val result = service.generate(invoiceId, "GPB")

        assertThat(result.qrInfo.contentQR).isEqualTo("base64")
        assertThat(result.qrInfo.mediaType).isEqualTo("image/png")
        assertThat(result.detais.recipient.kpp).isEmpty()
        assertThat(result.infoInvoice.accounts.agreementNumber).isEqualTo("FIRST")
        verify {
            qrGeneratorService.generateFileQR(
                match {
                    it.startsWith("ST00012|Name=Компания|PersonalAcc=40701") &&
                        it.contains("|Sum=15452|PayeeINN=7729503816|KPP=|LastName=Иванов|FirstName=Иван|MiddleName=Иванович") &&
                        it.contains("ДОГОВОРУ СТРАХОВАНИЯ FIRST от 01.01.2026") &&
                        !it.contains("SECOND")
                },
            )
        }
    }

    private fun subOrder(
        number: String,
        date: String,
    ) = SubOrderEntity(
        orderEntity = null,
        policyNumber = "policy",
        contractNumber = number,
        contractDate = Instant.parse(date),
    )

    private fun company() =
        CompanyDetailsQrEntity(
            id = UUID.randomUUID(),
            name = "Компания",
            personalAcc = "40701",
            bankName = "Банк",
            bic = "044525823",
            correspAcc = "30101",
            payeeInn = "7729503816",
            kpp = "",
            bank = "GPB",
        )
}
