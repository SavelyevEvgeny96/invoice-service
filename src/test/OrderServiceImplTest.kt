import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.properties.ServiceStatuses
import ru.sogaz.site.orderingService.service.impl.OrderServiceImpl
import ru.sogaz.site.orderingService.service.payment.PaymentService
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrderServiceImplTest {
    @Mock
    lateinit var orderDao: OrderDao

    @Mock
    private lateinit var paymentService: PaymentService

    @Mock
    lateinit var clientSystemDao: ClientSystemDao

    @Mock
    lateinit var orderManualMapper: OrderManualMapper

    private lateinit var service: OrderServiceImpl

    private val payBasePath = "https://pay.test/"
    private val skipSendingErrors = false

    @BeforeEach
    fun setUp() {
        service =
            OrderServiceImpl(
                orderDao = orderDao,
                paymentService = paymentService,
                orderManualMapper = orderManualMapper,
                clientSystemDao = clientSystemDao,
                payBasePath = payBasePath,
            )
    }

    @Test
    fun `createOrder returns success response`() {
        val request = CreateOrderCommand()

        val orderEntity = mock(OrderEntity::class.java)
        val savedOrder = mock(OrderEntity::class.java)
        val orderId = UUID.randomUUID()

        `when`(orderManualMapper.toOrderEntity(request, skipSendingErrors)).thenReturn(orderEntity)
        `when`(orderDao.save(orderEntity)).thenReturn(savedOrder)
        `when`(savedOrder.orderId).thenReturn(orderId)

        val response = service.createOrder(request)

        assertEquals(ServiceStatuses.STATUS_CODE_SUCCESS, response.code)
        assertEquals(orderId, response.data!!.orderId)
        assertEquals("$payBasePath$orderId", response.data!!.url)
    }

    @Test
    fun `createOrder calls mapper and dao`() {
        // given
        val request = CreateOrderCommand()

        val orderEntity = mock(OrderEntity::class.java)
        val savedOrder = mock(OrderEntity::class.java)

        `when`(orderManualMapper.toOrderEntity(request, skipSendingErrors)).thenReturn(orderEntity)
        `when`(orderDao.save(orderEntity)).thenReturn(savedOrder)
        `when`(savedOrder.orderId).thenReturn(UUID.randomUUID())

        // when
        service.createOrder(request)

        // then
        verify(orderManualMapper).toOrderEntity(request, skipSendingErrors)
        verify(orderDao).save(orderEntity)
    }
}
