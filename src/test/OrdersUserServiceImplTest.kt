import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.OrdersStatusIsPaidEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchNameEnum
import ru.sogaz.site.orderingService.service.impl.OrdersUserServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class OrdersUserServiceImplTest {
    @Mock
    lateinit var orderDao: OrderDao

    @Mock
    lateinit var subOrderDao: SubOrderDao

    @InjectMocks
    lateinit var service: OrdersUserServiceImpl

    private lateinit var orderEntity: OrderEntity
    private lateinit var subOrderEntity: SubOrderEntity

    @BeforeEach
    fun `инициализация_тестовых_данных`() {
        orderEntity =
            OrderEntity(
                orderId = UUID.randomUUID(),
                premiumAmount = BigDecimal("3500"),
                status = OrderStatusesEnum.NEW,
                recipientEmail = "",
                recipientPhone = "",
                paymentEndDate = Instant.now(),
                updateDate = Instant.now(),
                keyCard = "",
                recipientUserId = "",
                unifiedId = "",
                bank = "",
                policyholder = "dto.policyholder",
                paymentType = "",
                recurrent = false,
                saveCard = true,
                subscriptionId = "",
                createDate = Instant.now(),
            )

        subOrderEntity =
            SubOrderEntity(
                id = UUID.randomUUID(),
                policyId = "policy123",
                policyNumber = "SG-001",
                typeInsurance = "Ипотека",
                premiumAmount = BigDecimal("3500"),
                orderEntity = orderEntity,
                contractId = "",
                contractNumber = "",
                insuranceProgram = "",
                managerEmail = "",
                docType = "",
                channel = "",
                mainContractCheck = true,
                createDate = Instant.now(),
                policyDate = Instant.now(),
                updateDate = Instant.now(),
                contractDate = Instant.now(),
            )
    }

    @Test
    fun `должен вернуть orders с вложенными suborders`() {
        val request =
            OrdersUserRequest(
                searchName = OrdersUserSearchNameEnum.USER_ID,
                userId = "user1",
                status = OrdersStatusIsPaidEnum.UNPAID,
            )

        whenever(orderDao.findByRecipientUserId("user1")).thenReturn(listOf(orderEntity))
        whenever(subOrderDao.findByOrderId(orderEntity.orderId)).thenReturn(listOf(subOrderEntity))

        val response = service.findOrders(request)

        assertNotNull(response)
        val data = response.data
        assertNotNull(data)
        assertEquals(1, data.ordersList.size)
        val orderItem = data.ordersList.first()
        assertEquals(orderEntity.orderId, orderItem.orderId)
        assertEquals(orderEntity.premiumAmount, orderItem.premiumAmount)
        assertEquals(1, orderItem.subOrdersList.size)
        assertEquals(subOrderEntity.policyId, orderItem.subOrdersList.first().policyId)

        verify(orderDao, times(1)).findByRecipientUserId(eq("user1"))
        verify(subOrderDao, times(1)).findByOrderId(eq(orderEntity.orderId))
    }

    @Test
    fun `должен выбросить исключение если заказы не найдены`() {
        val request =
            OrdersUserRequest(
                searchName = OrdersUserSearchNameEnum.USER_ID,
                userId = "userX",
                status = OrdersStatusIsPaidEnum.UNPAID,
            )

        whenever(orderDao.findByRecipientUserId("userX")).thenReturn(emptyList())

        val ex =
            assertThrows<BusinessException> {
                service.findOrders(request)
            }

        assertTrue(ex is BusinessException)
        verify(orderDao, times(1)).findByRecipientUserId(eq("userX"))
        verify(subOrderDao, never()).findByOrderId(any())
    }

    @Test
    fun `должен выбросить BusinessException при ошибке в OrderDao`() {
        val request =
            OrdersUserRequest(
                searchName = OrdersUserSearchNameEnum.USER_ID,
                userId = "crash",
                status = OrdersStatusIsPaidEnum.UNPAID,
            )

        whenever(orderDao.findByRecipientUserId("crash")).thenThrow(RuntimeException("DB down"))

        val ex =
            assertThrows<BusinessException> {
                service.findOrders(request)
            }

        assertTrue(ex is BusinessException)
        verify(subOrderDao, never()).findByOrderId(any())
    }

    @Test
    fun `должен вернуть заказы даже если SubOrderDao выбрасывает исключение`() {
        val request =
            OrdersUserRequest(
                searchName = OrdersUserSearchNameEnum.USER_ID,
                userId = "user3",
                status = OrdersStatusIsPaidEnum.UNPAID,
            )

        whenever(orderDao.findByRecipientUserId("user3")).thenReturn(listOf(orderEntity))
        whenever(subOrderDao.findByOrderId(any())).thenThrow(RuntimeException("DB error"))

        val response = service.findOrders(request)

        assertEquals(1, response.data?.ordersList?.size)
        response.data
            ?.ordersList
            ?.first()
            ?.subOrdersList
            ?.isEmpty()
            ?.let { assertTrue(it) }
    }
}
