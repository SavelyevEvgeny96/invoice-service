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
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.service.impl.OrderServiceImpl
import ru.sogaz.site.orderingService.service.payment.PaymentService
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrderServiceImplTest {
    @Mock
    lateinit var orderDao: OrderDao

    @Mock
    lateinit var subOrderDao: SubOrderDao

    @Mock
    lateinit var paymentService: PaymentService

    @Mock
    lateinit var clientSystemDao: ClientSystemDao

    @Mock
    lateinit var orderManualMapper: OrderManualMapper

    @Mock
    lateinit var orderMapper: OrderMapper

    private lateinit var service: OrderServiceImpl
    private lateinit var request: CreateOrderCommand

    private val payBasePath = "https://pay.test/"

    @BeforeEach
    fun setUp() {
        request = mock(CreateOrderCommand::class.java)
        `when`(request.clientId).thenReturn("")
        `when`(request.subOrders).thenReturn(mutableListOf())

        service =
            OrderServiceImpl(
                orderDao = orderDao,
                paymentService = paymentService,
                orderMapper = orderMapper,
                clientSystemDao = clientSystemDao,
                payBasePath = payBasePath,
                subOrderDao = subOrderDao,
                orderManualMapper = orderManualMapper,
            )
    }

    @Test
    fun `createOrderInternal returns created order result`() {
        val orderEntity = mock(OrderEntity::class.java)
        val savedOrder = mock(OrderEntity::class.java)
        val orderId = UUID.randomUUID()

        `when`(orderManualMapper.toOrderEntity(request, false)).thenReturn(orderEntity)
        `when`(orderDao.save(orderEntity)).thenReturn(savedOrder)
        `when`(savedOrder.orderId).thenReturn(orderId)
        `when`(orderManualMapper.toSubOrderEntities(savedOrder, request.subOrders)).thenReturn(emptyList())

        val result = service.createOrderInternal(request)

        assertEquals(orderId, result.orderId)
        assertEquals("$payBasePath$orderId", result.paymentUrl)
    }

    @Test
    fun `createOrderInternal calls mapper and dao`() {
        val orderEntity = mock(OrderEntity::class.java)
        val savedOrder = mock(OrderEntity::class.java)
        val orderId = UUID.randomUUID()

        `when`(orderManualMapper.toOrderEntity(request, false)).thenReturn(orderEntity)
        `when`(orderDao.save(orderEntity)).thenReturn(savedOrder)
        `when`(savedOrder.orderId).thenReturn(orderId)
        `when`(orderManualMapper.toSubOrderEntities(savedOrder, request.subOrders)).thenReturn(emptyList())

        service.createOrderInternal(request)

        verify(orderManualMapper).toOrderEntity(request, false)
        verify(orderDao).save(orderEntity)
        verify(orderManualMapper).toSubOrderEntities(savedOrder, request.subOrders)
        verify(subOrderDao).saveAll(emptyList())
    }
}
