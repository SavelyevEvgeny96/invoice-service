package ru.sogaz.site.orderingService.service.order

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
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
import ru.sogaz.site.orderingService.dto.data.ClientCardDetails
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.payment.CompletedPaymentMapper
import ru.sogaz.site.orderingService.mappers.payment.CompletedPaymentMapperImpl
import ru.sogaz.site.orderingService.mappers.payment.PaymentOperationStatusConverterImpl
import ru.sogaz.site.orderingService.service.order.impl.OrderStatusServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [CompletedPaymentMapperImpl::class, PaymentOperationStatusConverterImpl::class])
class OrderStatusServiceTest {
    companion object {
        private const val TEST_CLIENT_EMAIL = "test@example.com"
        private const val TEST_CONTRACT_NUMBER = "CONT123"
        private const val KEY_CARD = "KEY_CARD"
        private const val PAYMENT_TYPE = "PAYMENT_TYPE"
        private const val PAYER_IP = "PAYER_IP"
        private const val PAYMENT_BANK_ID = "PAYMENT_BANK_ID"
        private val BANK = BankEnum.GPB.name

        private val amount: BigDecimal = BigDecimal.TEN
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @Autowired
    private lateinit var completedPaymentMapper: CompletedPaymentMapper

    private lateinit var orderStatusService: OrderStatusService

    private lateinit var validOrderId: UUID

    @RelaxedMockK
    private lateinit var validOrder: OrderEntity

    @RelaxedMockK
    private lateinit var validSubOrder: SubOrderEntity
    private lateinit var validCompletedPayment: CompletedPaymentData

    @BeforeEach
    fun beforeEach() {
        orderStatusService =
            OrderStatusServiceImpl(
                orderDao = orderDao,
                completedPaymentMapper = completedPaymentMapper,
            )

        initOrdersTestData()

        every { orderDao.findById(any()) } returns validOrder
        every { orderDao.save(any()) } returnsArgument 0
    }

    @Test
    fun `updatePaidOrder should correctly update order`() {
        orderStatusService.updatePaidOrder(validCompletedPayment)

        verify { validOrder.paymentType = validCompletedPayment.paymentType }
        verify { validOrder.bank = validCompletedPayment.bank }
        verify { validOrder.keyCard = validCompletedPayment.card?.cardId }
    }

    @Test
    fun `updatePaidOrder should throw an error if order not found`() {
        every { orderDao.findById(any()) } returns null

        assertThrows<OrderNotFoundException> { orderStatusService.updatePaidOrder(validCompletedPayment) }
    }

    private fun initOrdersTestData() {
        validOrderId = UUID.randomUUID()
        val subId = UUID.randomUUID()

        every { validSubOrder.id } returns subId
        every { validSubOrder.contractNumber } returns TEST_CONTRACT_NUMBER
        every { validSubOrder.premiumAmount } returns amount
        every { validSubOrder.orderEntity } returns validOrder

        every { validOrder.orderId } returns validOrderId
        every { validOrder.recipientEmail } returns TEST_CLIENT_EMAIL
        every { validOrder.premiumAmount } returns amount
        every { validOrder.subOrders } returns mutableListOf(validSubOrder)

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
                externalErrorCode = null,
                paymentBankId = PAYMENT_BANK_ID,
                payerIp = PAYER_IP,
            )
    }
}
