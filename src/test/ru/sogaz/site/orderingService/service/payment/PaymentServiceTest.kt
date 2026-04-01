package ru.sogaz.site.orderingService.service.payment

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.mappers.payment.PaymentPurposeMapperImpl
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapperImpl
import ru.sogaz.site.orderingService.service.payment.impl.PaymentServiceImpl
import ru.sogaz.site.payment.client.api.PayV2Api
import ru.sogaz.site.payment.client.model.BankPaymentPageData
import ru.sogaz.site.payment.client.model.ResponseBankPaymentPageData
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [PaymentServiceMapperImpl::class, PaymentPurposeMapperImpl::class])
class PaymentServiceTest {
    companion object {
        private const val TEST_CONTRACT_NUMBER = "contract-number"
        private const val TEST_PAYMENT_PAGE_URL = "http://sogaz.ru"
    }

    @MockK
    private lateinit var payV2Api: PayV2Api

    @Autowired
    private lateinit var paymentServiceMapper: PaymentServiceMapper

    private lateinit var paymentService: PaymentService

    @RelaxedMockK
    private lateinit var payQueryParams: PayQueryParams

    private lateinit var dataPay: ResponseBankPaymentPageData

    private lateinit var order: OrderEntity

    @BeforeEach
    fun beforeEach() {
        paymentService = PaymentServiceImpl(paymentServiceMapper, payV2Api)
        order = createOrder()
        dataPay = ResponseBankPaymentPageData().apply { data = BankPaymentPageData().apply { paymentPageUrl = TEST_PAYMENT_PAGE_URL } }
    }

    @Test
    fun `should correctly map order to payRequest`() {
        val payRequest = paymentServiceMapper.orderToCardPayRequest(order, payQueryParams)

        assertThat(payRequest)
            .returns(order.premiumAmount) { it.amount }
            .returns(order.orderId) { it.orderId }

        assertThat(payRequest.payItems.values.first())
            .contains(TEST_CONTRACT_NUMBER)

        assertThat(payRequest.description)
            .contains(TEST_CONTRACT_NUMBER)
    }

    @Test
    fun `should correctly return dataPay`() {
        every { payV2Api.pay(any()) } returns dataPay

        val paymentPage = paymentService.payCard(order, payQueryParams)

        assertThat(paymentPage)
            .returns(TEST_PAYMENT_PAGE_URL, PaymentPage::uri)
    }

    private fun createOrder() =
        OrderEntity(
            orderId = UUID.randomUUID(),
            unifiedId = "",
            clientId = "",
            bank = BankEnum.GPB.name,
            policyholder = "",
            paymentType = "",
            subscriptionId = "",
            keyCard = "",
            saveCard = false,
            status = OrderStatusesEnum.NEW,
            recurrent = false,
            paymentEndDate = null,
            premiumAmount = BigDecimal.TEN,
            recipientEmail = "",
            recipientPhone = null,
            recipientUserId = null,
            createDate = null,
            updateDate = null,
            urlToReturn = "",
            urlToDecline = "",
            regCard = false,
            refundDate = null,
            queueStatusResultName = "",
            skipSendingQueue = false,
            skipSendingReceipt = false,
            depersonalization = false,
            urlPayPageShort = null,
            typePaymentOperation = null,
        ).apply {
            subOrders.add(createSubOrder())
        }

    private fun createSubOrder() =
        SubOrderEntity(
            id = UUID.randomUUID(),
            orderEntity = null,
            policyId = "",
            policyNumber = "",
            contractId = "",
            contractNumber = TEST_CONTRACT_NUMBER,
            docType = "",
            channel = "",
            mainContractCheck = true,
            insuranceProgram = "",
            typeInsurance = "",
            premiumAmount = BigDecimal.TEN,
            managerEmail = "",
            createDate = null,
            updateDate = null,
            contractDate = Instant.now(),
            policyDate = null,
            typeOperation = null,
        )
}
