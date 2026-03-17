package ru.sogaz.site.orderingService.service.payment

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dto.data.ClientCardDetails
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.payment.PaymentOperationMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentOperationMapperImpl
import ru.sogaz.site.orderingService.service.payment.impl.PaymentOperationsServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [PaymentOperationMapperImpl::class])
class PaymentOperationsServiceTest {
    companion object {
        private const val TEST_CLIENT_EMAIL = "test@example.com"
        private const val TEST_CONTRACT_NUMBER = "CONT123"
        private const val KEY_CARD = "KEY_CARD"
        private const val PAYMENT_TYPE = "PAYMENT_TYPE"
        private val BANK = BankEnum.GPB.name

        private val amount: BigDecimal = BigDecimal.TEN
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @Autowired
    private lateinit var paymentOperationMapper: PaymentOperationMapper

    @MockK
    private lateinit var paymentOperationDao: PaymentOperationDao

    private lateinit var paymentOperationsService: PaymentOperationsService

    private lateinit var validOrderId: UUID

    @RelaxedMockK
    private lateinit var validOrder: OrderEntity

    @RelaxedMockK
    private lateinit var validSubOrder: SubOrderEntity
    private lateinit var validCompletedPayment: CompletedPaymentData

    @BeforeEach
    fun beforeEach() {
        paymentOperationsService =
            PaymentOperationsServiceImpl(
                orderDao = orderDao,
                paymentOperationMapper = paymentOperationMapper,
                paymentOperationDao = paymentOperationDao,
            )

        initOrdersTestData()

        every { orderDao.findById(any()) } returns validOrder
        every { paymentOperationDao.save(any()) } returnsArgument 0
    }

    @Test
    fun `updatePaidOrder should correctly update order`() {
        val paymentOperation = slot<PaymentOperationEntity>()

        paymentOperationsService.saveOperation(validCompletedPayment)

        verify { paymentOperationDao.save(capture(paymentOperation)) }

        assertThat(paymentOperation.captured)
            .returns(validOrder) { it.orderEntity }
            .returns(validCompletedPayment.paymentType) { it.type }
            .returns(validCompletedPayment.bank) { it.bank.name }
            .returns(validCompletedPayment.depersonalization) { it.depersonalization }
    }

    @Test
    fun `updatePaidOrder should throw an error if order not found`() {
        every { orderDao.findById(any()) } returns null

        assertThrows<OrderNotFoundException> { paymentOperationsService.saveOperation(validCompletedPayment) }
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
                card = ClientCardDetails(KEY_CARD),
                bank = BANK,
                paymentType = PAYMENT_TYPE,
                payDate = Instant.now(),
                errorText = null,
            )
    }
}
