package ru.sogaz.site.orderingService.service.receipt

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.ReceiptDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptClientInfoMapperImpl
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptItemMapperImpl
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptMapper
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptMapperImpl
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptPaymentMapperImpl
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptTotalAmountMapperImpl
import ru.sogaz.site.orderingService.service.receipt.impl.ReceiptServiceImpl
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateResponse
import ru.sogaz.site.payment.receipt.client.model.ResponsePaymentReceiptCreateResponse
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(
    value = [
        ReceiptMapperImpl::class,
        ReceiptTotalAmountMapperImpl::class,
        ReceiptItemMapperImpl::class,
        ReceiptPaymentMapperImpl::class,
        ReceiptClientInfoMapperImpl::class,
    ],
)
class ReceiptServiceTest {
    companion object {
        private const val SUCCESS_STATUS = "SUCCESS"
        private const val FAILED_STATUS = "FAILED"
        private const val TEST_CLIENT_EMAIL = "test@example.com"
        private const val TEST_CONTRACT_NUMBER = "CONT123"

        private val amount: BigDecimal = BigDecimal.TEN
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var receiptDao: ReceiptDao

    @RelaxedMockK
    private lateinit var receiptClient: ReceiptClient

    @Autowired
    private lateinit var receiptMapper: ReceiptMapper

    private lateinit var receiptService: ReceiptService

    private lateinit var validOrderId: UUID

    @RelaxedMockK
    private lateinit var validOrder: OrderEntity

    @RelaxedMockK
    private lateinit var validSubOrder: SubOrderEntity
    private lateinit var validCompletedPayment: CompletedPaymentData

    @BeforeEach
    fun beforeEach() {
        receiptService =
            ReceiptServiceImpl(
                orderDao = orderDao,
                receiptMapper = receiptMapper,
                receiptClient = receiptClient,
                receiptDao = receiptDao,
            )

        initOrdersTestData()

        every { orderDao.findById(any()) } returns validOrder
        every { orderDao.save(any()) } returnsArgument 0
    }

    @Test
    fun `sendReceipt should send valid request`() {
        val requestSlot = slot<PaymentReceiptCreateRequest>()

        receiptService.sendReceipt(validCompletedPayment)

        verify(exactly = 1) { receiptClient.sendReceiptToQueue(capture(requestSlot)) }

        requestSlot.captured
            .run(Assertions::assertThat)
            .returns(TEST_CLIENT_EMAIL) { it.client.email }
            .returns(validCompletedPayment.depersonalization) { it.depersonalization }
            .returns(amount) { it.total }
    }

    @Test
    fun `sendReceipt should throw exception if order not found`() {
        every { orderDao.findById(any()) } returns null

        assertThrows<OrderNotFoundException> {
            receiptService.sendReceipt(validCompletedPayment)
        }
    }

    private fun initOrdersTestData() {
        validOrderId = UUID.randomUUID()

        validSubOrder =
            SubOrderEntity(
                contractNumber = TEST_CONTRACT_NUMBER,
                premiumAmount = amount,
                id = UUID.randomUUID(),
                orderEntity = validOrder,
                policyId = "",
                contractId = null,
                docType = null,
                channel = null,
                mainContractCheck = true,
                insuranceProgram = null,
                typeInsurance = null,
                managerEmail = null,
                createDate = null,
                updateDate = null,
                contractDate = null,
                policyDate = null,
            )
        validOrder =
            OrderEntity(
                orderId = validOrderId,
                premiumAmount = amount,
                recipientEmail = TEST_CLIENT_EMAIL,
                unifiedId = null,
                bank = null,
                policyholder = null,
                paymentType = null,
                subscriptionId = null,
                keyCard = null,
                urlToReturn = null,
                urlToDecline = null,
                saveCard = null,
                recurrent = null,
                paymentEndDate = null,
                refundDate = null,
                recipientPhone = null,
                recipientUserId = null,
                queueStatusResultName = null,
                depersonalization = false,
                createDate = null,
                updateDate = null,
            ).apply {
                subOrders.add(validSubOrder)
            }
        validCompletedPayment =
            CompletedPaymentData(
                paymentId = UUID.randomUUID(),
                orderId = validOrderId,
                totalAmount = amount,
                depersonalization = true,
                status = PaymentOperationStateEnum.SUCCESS,
                operationType = OperationTypeEnum.PAY,
                card = null,
                bank = "gpb",
                paymentType = "card",
                payDate = Instant.now(),
                errorText = null,
            )
    }

    private fun buildSuccessReceiptServiceResponse() = buildReceiptServiceResponse(SUCCESS_STATUS, 200)

    private fun buildFailedReceiptServiceResponse() = buildReceiptServiceResponse(FAILED_STATUS, 500)

    private fun buildReceiptServiceResponse(
        status: String,
        code: Int,
    ) = ResponsePaymentReceiptCreateResponse()
        .status(status)
        .responseUuid(UUID.randomUUID())
        .code(code)
        .traceId("222")
        .data(PaymentReceiptCreateResponse().state("222").externalId("222"))
}
