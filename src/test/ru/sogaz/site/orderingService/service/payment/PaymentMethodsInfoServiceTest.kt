package ru.sogaz.site.orderingService.service.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.FileQR
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse
import ru.sogaz.site.orderingService.dto.response.KeycloakTokenResponse
import ru.sogaz.site.orderingService.entity.DefaultPaymentMethodEntity
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.repository.DefaultPaymentMethodRepository
import ru.sogaz.site.orderingService.service.payment.impl.PayInfoServiceImpl
import ru.sogaz.site.orderingService.service.payment.impl.PaymentMethodsResolverImpl
import ru.sogaz.site.orderingService.apiDoc.integretion.KeycloakApi
import ru.sogaz.site.orderingService.apiDoc.integretion.LKApi
import java.net.URI
import java.util.UUID

class PaymentMethodsInfoServiceTest {
    private val order = OrderEntity(queueStatusResultName = "queue").apply { orderId = UUID.randomUUID() }
    private val params = PayQueryParams(urlToReturn = "https://return.example/path?a=b")

    private fun payInfoService(
        paymentService: PaymentService = mockk(),
        builder: PaymentMethodURIBuilder = mockk(),
        generator: QrGeneratorService = mockk(),
        lkApi: LKApi = mockk(),
        keycloakApi: KeycloakApi = mockk(),
    ) =
        PayInfoServiceImpl(
            paymentService,
            builder,
            generator,
            lkApi,
            keycloakApi,
            "invoice-client",
            "secret",
            "https://gateway.example/",
            300,
        )

    @Test
    fun `order payment methods take precedence over defaults`() {
        val repository = mockk<DefaultPaymentMethodRepository>()
        order.paymentMethodList = setOf(PaymentMethod.CARD)

        assertThat(PaymentMethodsResolverImpl(repository).resolve(order)).containsExactly(PaymentMethod.CARD)
        verify(exactly = 0) { repository.findAllByAvailabilityTrue() }
    }

    @Test
    fun `enabled default payment methods are used for empty order list`() {
        val repository = mockk<DefaultPaymentMethodRepository>()
        order.paymentMethodList = emptySet()
        every { repository.findAllByAvailabilityTrue() } returns
            listOf(
                DefaultPaymentMethodEntity(name = "CARD", availability = true),
                DefaultPaymentMethodEntity(name = "SBP", availability = true),
            )

        assertThat(PaymentMethodsResolverImpl(repository).resolve(order)).containsExactlyInAnyOrder(PaymentMethod.CARD, PaymentMethod.SBP)
    }

    @Test
    fun `only card creates card link and does not call SBP`() {
        val paymentService = mockk<PaymentService>()
        val builder = mockk<PaymentMethodURIBuilder>()
        val generator = mockk<QrGeneratorService>()
        val cardUrl = URI("https://gateway/payment/pay/${order.orderId}?urlToReturn=https%3A%2F%2Freturn.example")
        every { builder.buildPayCardURI(order.orderId!!, params) } returns cardUrl

        val result =
            payInfoService(paymentService, builder, generator)
                .getInfo(order, params, setOf(PaymentMethod.CARD))

        assertThat(result.first).isEqualTo(cardUrl)
        assertThat(result.second).isNull()
        verify(exactly = 0) { paymentService.paySbp(any(), any()) }
        verify(exactly = 0) { generator.generateFileQR(any<URI>(), any()) }
    }

    @Test
    fun `SBP uses payment page URL and generated QR`() {
        val paymentService = mockk<PaymentService>()
        val builder = mockk<PaymentMethodURIBuilder>()
        val generator = mockk<QrGeneratorService>()
        val paymentUrl = "https://bank.example/payment-page"
        val qr = FileQR("base64", "image/png")
        every { paymentService.paySbp(order, params) } returns PaymentPage(paymentUrl)
        every { generator.generateFileQR(URI(paymentUrl), 300) } returns qr

        val result =
            payInfoService(paymentService, builder, generator)
                .getInfo(order, params, setOf(PaymentMethod.SBP))

        assertThat(result.first).isNull()
        assertThat(result.second?.urlPay).isEqualTo(paymentUrl)
        assertThat(result.second?.fileQR).isEqualTo(qr)
    }

    @Test
    fun `SBP registration failure does not fail payment page`() {
        val paymentService = mockk<PaymentService>()
        val builder = mockk<PaymentMethodURIBuilder>()
        val generator = mockk<QrGeneratorService>()
        every { paymentService.paySbp(order, params) } throws IllegalStateException("payment unavailable")

        val result =
            payInfoService(paymentService, builder, generator)
                .getInfo(order, params, setOf(PaymentMethod.SBP))

        assertThat(result.first).isNull()
        assertThat(result.second).isNull()
        verify(exactly = 0) { generator.generateFileQR(any<URI>(), any()) }
    }

    @Test
    fun `GID info requests token before LK cards`() {
        val keycloakApi = mockk<KeycloakApi>()
        val lkApi = mockk<LKApi>()
        val gidId = "gid-123"
        val response = GidAuthResponse(gidId, "refresh-token")
        every { keycloakApi.getToken(any()) } returns KeycloakTokenResponse("access-token")
        every { lkApi.getGidId(gidId, "Bearer access-token") } returns response

        val result = payInfoService(lkApi = lkApi, keycloakApi = keycloakApi).getGidIdInfo(
            PayQueryParams(gidId = gidId),
            order.orderId!!,
        )

        assertThat(result.first).isEqualTo(response)
        assertThat(result.second).isEqualTo("https://gateway.example/v1/invoice/paycardgid/${order.orderId}")
        verifyOrder {
            keycloakApi.getToken(any())
            lkApi.getGidId(gidId, "Bearer access-token")
        }
    }

    @Test
    fun `missing token skips LK request and returns empty GID info`() {
        val keycloakApi = mockk<KeycloakApi>()
        val lkApi = mockk<LKApi>()
        every { keycloakApi.getToken(any()) } returns KeycloakTokenResponse(null)

        val result = payInfoService(lkApi = lkApi, keycloakApi = keycloakApi).getGidIdInfo(
            PayQueryParams(gidId = "gid-123"),
            order.orderId!!,
        )

        assertThat(result.first).isNull()
        assertThat(result.second).isNull()
        verify(exactly = 0) { lkApi.getGidId(any(), any()) }
    }
}
