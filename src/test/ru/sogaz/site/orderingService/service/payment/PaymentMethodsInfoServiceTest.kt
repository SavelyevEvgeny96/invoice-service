package ru.sogaz.site.orderingService.service.payment

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.FileQR
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.MediaTypeValue
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.mappers.order.InvoiceMetaInfoMapper
import ru.sogaz.site.orderingService.mappers.order.InvoiceMetaInfoMapperImpl
import ru.sogaz.site.orderingService.mappers.payment.PaymentMethodsMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentMethodsMapperImpl
import ru.sogaz.site.orderingService.service.order.impl.OrderPaymentPageServiceImpl
import ru.sogaz.site.orderingService.service.payment.impl.PayInfoServiceImpl
import ru.sogaz.site.orderingService.service.payment.impl.PaymentMethodURIBuilderImpl
import java.math.BigDecimal
import java.net.URI
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [PaymentMethodsMapperImpl::class, InvoiceMetaInfoMapperImpl::class])
class PaymentMethodsInfoServiceTest {
    companion object {
        private const val QR_CONTENT = "QR Content"
        private const val BANK_PAYMENT_URL = "http://some-bank.ru/payment"
        private const val BASE_PAYMENT_CARD_PAY_PATH = "http://payment-card-test.ru/"
        private const val BASE_PAYMENT_SBP_PAY_PATH = "http://payment-sbp-test.ru/"

        private const val FIRST_SUB_ORDER_POLICY_NUMBER = "first-policy-number"
        private const val FIRST_SUB_ORDER_CONTRACT_NUMBER = "first-contract-number"
        private const val FIRST_SUB_ORDER_TYPE_INSURANCE = "first-test-insurance"
        private const val FIRST_SUB_ORDER_INSURANCE_PROGRAM = "first-test-program"

        private const val SECOND_SUB_ORDER_POLICY_NUMBER = "second-policy-number"
        private const val SECOND_SUB_ORDER_CONTRACT_NUMBER = "second-contract-number"
        private const val SECOND_SUB_ORDER_TYPE_INSURANCE = "second-test-insurance"
        private const val SECOND_SUB_ORDER_INSURANCE_PROGRAM = "second-test-program"

        private const val RETURN_URL = "http://www.sogaz.com"

        private val orderTestAmount = BigDecimal.TEN

        private val validFileQR = FileQR(QR_CONTENT, MediaTypeValue.IMAGE_PNG_VALUE.value)
        private val validBankPaySbp = PaySbp(BANK_PAYMENT_URL, validFileQR)

        private val payQueryParams = PayQueryParams(RETURN_URL)
    }

    @MockK
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var paymentMethodsMapper: PaymentMethodsMapper

    @Autowired
    private lateinit var invoiceMetaInfoMapper: InvoiceMetaInfoMapper

    @MockK
    private lateinit var qrGeneratorService: QrGeneratorService

    @MockK
    private lateinit var orderDao: OrderDao

    private lateinit var payInfoService: PayInfoService
    private lateinit var orderPaymentPageService: OrderPaymentPageServiceImpl

    private lateinit var validOrderUUID: UUID
    private lateinit var validOrder: OrderEntity
    private lateinit var firstSubOrder: SubOrderEntity
    private lateinit var secondSubOrder: SubOrderEntity
    private lateinit var payURITemplate: URI
    private lateinit var sbpPayURITemplate: String

    @BeforeEach
    fun beforeEach() {
        validOrderUUID = UUID.randomUUID()
        payURITemplate = initPayURITemplate()
        sbpPayURITemplate = initSbpPayURITemplate().toString()
        initTestOrder()

        every { orderDao.findById(validOrderUUID) } returns validOrder
        payInfoService = initInfoPageService()
        orderPaymentPageService = initOrderPaymentPageService()
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo without sbp pay info`() {
        deactivateSbp()

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(payURITemplate, DataOrderPaymentPageInfo::urlPayBank)
            .returns(orderTestAmount.toString(), DataOrderPaymentPageInfo::premiumAmount)
            .returns(null, DataOrderPaymentPageInfo::paySbp)

        verify(exactly = 0) { qrGeneratorService.generateFileQR(any<URI>(), any()) }
        verify(exactly = 0) { paymentService.payQrSbp(validOrder, payQueryParams) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return empty pay sbp if the request to the bank has been failed`() {
        every { paymentService.payQrSbp(validOrder, any()) } returns null

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(payURITemplate, DataOrderPaymentPageInfo::urlPayBank)
            .returns(orderTestAmount.toString(), DataOrderPaymentPageInfo::premiumAmount)
            .returns(null, DataOrderPaymentPageInfo::paySbp)
    }

    @Test
    fun `getOrderPaymentPageInfo should return empty accounts for order without suborders`() {
        every { paymentService.payQrSbp(validOrder, payQueryParams) } returns null
        every { validOrder.subOrders } returns mutableListOf()

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(orderTestAmount.toString(), DataOrderPaymentPageInfo::premiumAmount)
            .returns(emptyList(), DataOrderPaymentPageInfo::accounts)
            .returns(null, DataOrderPaymentPageInfo::paySbp)
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid accounts`() {
        deactivateSbp()

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo.accounts)
            .anyMatch {
                it.contractNumber == FIRST_SUB_ORDER_CONTRACT_NUMBER
                it.policyNumber == FIRST_SUB_ORDER_POLICY_NUMBER
                it.typeInsurance == FIRST_SUB_ORDER_TYPE_INSURANCE
                it.insuranceProgram == FIRST_SUB_ORDER_INSURANCE_PROGRAM
            }.anyMatch {
                it.contractNumber == SECOND_SUB_ORDER_CONTRACT_NUMBER
                it.policyNumber == SECOND_SUB_ORDER_POLICY_NUMBER
                it.typeInsurance == SECOND_SUB_ORDER_TYPE_INSURANCE
                it.insuranceProgram == SECOND_SUB_ORDER_INSURANCE_PROGRAM
            }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr requested from bank`() {
        every { paymentService.payQrSbp(validOrder, payQueryParams) } returns validBankPaySbp

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(BANK_PAYMENT_URL) { it.paySbp?.urlPay }
            .returns(validFileQR) { it.paySbp?.fileQR }

        verify(exactly = 0) { qrGeneratorService.generateFileQR(any<URI>(), any()) }
        verify { paymentService.payQrSbp(validOrder, payQueryParams) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid paySbp with active qrGenerator`() {
        activateQrGenerator()
        every { qrGeneratorService.generateFileQR(any<URI>(), any()) } returns validFileQR

        val dataOrderPaymentPageInfo = orderPaymentPageService.getPaymentPage(validOrderUUID, payQueryParams)

        assertThat(dataOrderPaymentPageInfo)
            .returns(validOrderUUID, DataOrderPaymentPageInfo::orderId)
            .returns(sbpPayURITemplate) { it.paySbp?.urlPay }
            .returns(validFileQR) { it.paySbp?.fileQR }

        verify { qrGeneratorService.generateFileQR(any<URI>(), any()) }
    }

    private fun initInfoPageService() =
        PayInfoServiceImpl(
            paymentService = paymentService,
            paymentMethodURIBuilder = PaymentMethodURIBuilderImpl(BASE_PAYMENT_CARD_PAY_PATH, BASE_PAYMENT_SBP_PAY_PATH),
            qrGeneratorService = qrGeneratorService,
            isSbpActive = true,
            isQrGeneratorActive = false,
            qrCodeSize = 512,
        )

    private fun initOrderPaymentPageService() =
        OrderPaymentPageServiceImpl(
            orderDao = orderDao,
            payInfoService = payInfoService,
            paymentMethodsMapper = paymentMethodsMapper,
            invoiceMetaInfoMapper = invoiceMetaInfoMapper,
        )

    private fun initTestOrder() {
        firstSubOrder = mockk()
        firstSubOrder.apply {
            every { policyNumber } returns FIRST_SUB_ORDER_POLICY_NUMBER
            every { contractNumber } returns FIRST_SUB_ORDER_CONTRACT_NUMBER
            every { typeInsurance } returns FIRST_SUB_ORDER_TYPE_INSURANCE
            every { insuranceProgram } returns FIRST_SUB_ORDER_INSURANCE_PROGRAM
        }
        secondSubOrder = mockk()
        secondSubOrder.apply {
            every { policyNumber } returns SECOND_SUB_ORDER_POLICY_NUMBER
            every { contractNumber } returns SECOND_SUB_ORDER_CONTRACT_NUMBER
            every { typeInsurance } returns SECOND_SUB_ORDER_TYPE_INSURANCE
            every { insuranceProgram } returns SECOND_SUB_ORDER_INSURANCE_PROGRAM
        }
        validOrder = mockk()
        validOrder.apply {
            every { orderId } returns validOrderUUID
            every { status } returns OrderStatusesEnum.NEW
            every { subOrders } returns mutableListOf(firstSubOrder, secondSubOrder)
            every { premiumAmount } returns orderTestAmount
        }
    }

    private fun initPayURITemplate() =
        URI.create("${BASE_PAYMENT_CARD_PAY_PATH}$validOrderUUID?urlToReturn=${RETURN_URL}&depersonalization=false")

    private fun initSbpPayURITemplate() =
        URI.create("${BASE_PAYMENT_SBP_PAY_PATH}$validOrderUUID?urlToReturn=${RETURN_URL}&depersonalization=false")

    private fun activateQrGenerator() = ReflectionTestUtils.setField(payInfoService, "isQrGeneratorActive", true)

    private fun deactivateSbp() = ReflectionTestUtils.setField(payInfoService, "isSbpActive", false)
}
