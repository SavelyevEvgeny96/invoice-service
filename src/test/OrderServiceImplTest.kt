import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.ApiVersionEnum
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.service.order.impl.OrderServiceImpl
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.shortLinks.ShortLinksIntegration
import ru.sogaz.site.shortlinks.client.model.ShortLinkRequest
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    lateinit var shortLinksIntegration: ShortLinksIntegration

    private lateinit var service: OrderServiceImpl
    private lateinit var command: CreateOrderCommand

    private val payBasePath = "https://pay.test/"
    private val hostNameApp = "https://pay.test2"
    private val paymentUrlSuffix = "/payment/p/"

    @BeforeEach
    fun setUp() {
        command = mock()

        whenever(command.clientId).thenReturn("")
        whenever(command.subOrders).thenReturn(mutableListOf())
        whenever(command.versionApi).thenReturn(ApiVersionEnum.V1)

        // не вызываем command.clientId внутри whenever(...)
        whenever(clientSystemDao.findBySystemCode("")).thenReturn(null)

        service =
            OrderServiceImpl(
                orderDao = orderDao,
                paymentService = paymentService,
                clientSystemDao = clientSystemDao,
                payBasePath = payBasePath,
                subOrderDao = subOrderDao,
                orderManualMapper = orderManualMapper,
                hostNameApp = hostNameApp,
                paymentUrlSuffix = paymentUrlSuffix,
                shortLinksIntegration = shortLinksIntegration,
            )
    }

    @Test
    fun `createOrderInternal returns created order result for V1 and does not call short links service`() {
        val orderEntity = mock<OrderEntity>()
        val savedOrder = mock<OrderEntity>()
        val orderId = UUID.randomUUID()

        whenever(savedOrder.orderId).thenReturn(orderId)
        whenever(command.versionApi).thenReturn(ApiVersionEnum.V1)
        whenever(orderManualMapper.toOrderEntity(command, false)).thenReturn(orderEntity)
        whenever(orderDao.save(any())).thenReturn(savedOrder)
        whenever(orderManualMapper.toSubOrderEntities(savedOrder, command.subOrders)).thenReturn(emptyList())

        val result = service.createOrderInternal(command)

        assertEquals(orderId, result.orderId)
        assertEquals("$payBasePath$orderId", result.paymentUrl)

        verify(orderManualMapper).toOrderEntity(command, false)
        verify(orderDao).save(orderEntity)
        verify(orderManualMapper).toSubOrderEntities(savedOrder, command.subOrders)
        verify(orderDao).save(savedOrder)
        verify(shortLinksIntegration, never()).createShortLink(any())
    }

    @Test
    fun `createOrderInternal for V2 calls short links service and saves short url into order`() {
        val orderId = UUID.randomUUID()
        val orderEntity = mock<OrderEntity>()
        val savedOrder = mock<OrderEntity>()

        whenever(command.versionApi).thenReturn(ApiVersionEnum.V2)
        whenever(orderManualMapper.toOrderEntity(command, false)).thenReturn(orderEntity)

        whenever(orderDao.save(any())).thenReturn(savedOrder)
        whenever(savedOrder.orderId).thenReturn(orderId)
        whenever(savedOrder.paymentEndDate).thenReturn(
            Instant.now().plusSeconds(5 * 24 * 60 * 60L),
        )
        whenever(orderManualMapper.toSubOrderEntities(savedOrder, command.subOrders)).thenReturn(emptyList())

        val result = service.createOrderInternal(command)

        assertEquals(orderId, result.orderId)
        assertEquals("$payBasePath$orderId", result.paymentUrl)

        val captor = argumentCaptor<ShortLinkRequest>()
        verify(shortLinksIntegration).createShortLink(captor.capture())

        val requestToShortLink = captor.firstValue
        assertNotNull(requestToShortLink)
        assertEquals("$hostNameApp$paymentUrlSuffix$orderId", requestToShortLink.longUrl)
        assertEquals(100, requestToShortLink.maxVisits)
        verify(orderDao).save(orderEntity)
        verify(orderDao).save(savedOrder)
    }
}
