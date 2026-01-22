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
import ru.sogaz.site.orderingService.mappers.payment.ContractInfoMapperImpl
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapperImpl
import ru.sogaz.site.orderingService.service.payment.impl.PaymentServiceImpl
import ru.sogaz.site.payment.client.api.PayV2Api
import ru.sogaz.site.payment.client.model.DataPay
import ru.sogaz.site.payment.client.model.PayRequest
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [PaymentServiceMapperImpl::class, ContractInfoMapperImpl::class])
class PaymentServiceTest {
    companion object {
        private const val TEST_CONTRACT_NUMBER = "contract-number"
        private val TEST_PAYMENT_PAGE_URL = URI.create("http://sogaz.ru")
    }

    @MockK
    private lateinit var payV2Api: PayV2Api

    @Autowired
    private lateinit var paymentServiceMapper: PaymentServiceMapper

    private lateinit var paymentService: PaymentService

    @RelaxedMockK
    private lateinit var payQueryParams: PayQueryParams

    private lateinit var dataPay: DataPay

    private lateinit var order: OrderEntity

    @BeforeEach
    fun beforeEach() {
        paymentService = PaymentServiceImpl(paymentServiceMapper, payV2Api)
        order = createOrder()
        dataPay = DataPay().apply { paymentPageUrl = TEST_PAYMENT_PAGE_URL }
    }

    @Test
    fun `should correctly map order to payRequest`() {
        val payRequest = paymentServiceMapper.orderToPayRequest(order)

        assertThat(payRequest)
            .returns(order.premiumAmount, PayRequest::getAmount)
            .returns(order.orderId, PayRequest::getOrderId)

        assertThat(payRequest.contractsInfo.first())
            .returns(TEST_CONTRACT_NUMBER) { it.contractNumber }
    }

    @Test
    fun `should correctly return dataPay`() {
        every { payV2Api.pay(any(), any(), any(), any(), any()) } returns dataPay

        val paymentPage = paymentService.payCard(order, payQueryParams)

        assertThat(paymentPage)
            .returns(TEST_PAYMENT_PAGE_URL, PaymentPage::uri)
    }

    private fun createOrder() =
        OrderEntity(
            orderId = UUID.randomUUID(),
            unifiedId = "",
            clientId = "",
            bank = BankEnum.GPB,
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
        )
}
