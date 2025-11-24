import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.request.PaymentData
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.impl.BuildBatchConsumerServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BuildBatchConsumerServiceImplTest {
    @Mock
    lateinit var orderDao: OrderDao

    @Mock
    lateinit var subOrderDao: SubOrderDao

    @Mock
    lateinit var props: RabbitProps

    @Mock
    lateinit var orderMapper: OrderMapper

    @Mock
    lateinit var paymentEventMapper: PaymentEventMapper

    @InjectMocks
    lateinit var service: BuildBatchConsumerServiceImpl

    private lateinit var dto: OrderPayloadDto
    private lateinit var orderEntity: OrderEntity
    private lateinit var subOrderEntity: SubOrderEntity
    private lateinit var paymentEvent: PaymentCreatedEvent

    @BeforeEach
    fun `инициализация данных`() {
        val subOrderDto =
            SubOrderDto(
                policyId = "policy123",
                policyNumber = "PN-001",
                contractId = "CID-001",
                contractNumber = "CN-001",
                insuranceProgram = "Life",
                typeInsurance = "Personal",
                premiumAmountDto = BigDecimal.TEN,
                operationId = "opId",
            )

        dto =
            OrderPayloadDto(
                metaInfo = emptyList(),
                orderId = UUID.randomUUID().toString(),
                recipientEmail = "client@mail.com",
                recipientPhone = "+79998887766",
                recipientUserId = "user123",
                recipientGdId = "gd999",
                recurrent = false,
                keyCard = null,
                saveCard = null,
                managerEmail = "manager@mail.com",
                orderEndDate = Instant.now(),
                subOrders = listOf(subOrderDto),
            )

        orderEntity =
            OrderEntity(
                orderId = UUID.fromString(dto.orderId),
                recipientEmail = dto.recipientEmail ?: "",
                recipientPhone = dto.recipientPhone ?: "",
                premiumAmount = BigDecimal.TEN,
                paymentEndDate = dto.orderEndDate,
                updateDate = Instant.now(),
                recurrent = dto.recurrent,
                keyCard = dto.keyCard,
                saveCard = dto.saveCard,
                recipientUserId = dto.recipientUserId,
                recipientUserGdId = dto.recipientGdId,
            )

        subOrderEntity =
            SubOrderEntity(
                orderEntity = orderEntity,
                policyId = subOrderDto.policyId,
                policyNumber = subOrderDto.policyNumber,
                contractId = subOrderDto.contractId,
                contractNumber = subOrderDto.contractNumber,
                insuranceProgram = subOrderDto.insuranceProgram,
                typeInsurance = subOrderDto.typeInsurance,
                premiumAmount = subOrderDto.premiumAmountDto,
                managerEmail = dto.managerEmail,
            )

        paymentEvent =
            PaymentCreatedEvent(
                eventType = "type",
                timestamp = "timestamp",
                data =
                    PaymentData(
                        orderId = orderEntity.orderId!!,
                        recurrent = orderEntity.recurrent ?: false,
                        premiumAmount = orderEntity.premiumAmount,
                        saveCard = orderEntity.saveCard,
                        keyCard = orderEntity.keyCard,
                        recipientEmail = orderEntity.recipientEmail,
                        recipientPhone = orderEntity.recipientPhone,
                        dateCreate = orderEntity.updateDate?.toString(),
                        dateEnd = orderEntity.paymentEndDate?.toString(),
                    ),
            )

        whenever(props.routingKeyPayment).thenReturn("type")
        whenever(orderMapper.toOrderEntity(dto)).thenReturn(orderEntity)
        whenever(orderMapper.toSubOrderEntity(any(), eq(orderEntity), any())).thenReturn(subOrderEntity)
        whenever(paymentEventMapper.toPaymentEvent(any(), any(), any())).thenReturn(paymentEvent)
    }

    @Test
    fun `должен успешно обработать пачку заказов`() {
        val result = service.upsertBatch(listOf(dto))

        verify(orderDao).upsertOrders(listOf(orderEntity))
        verify(subOrderDao).upsertSubOrders(listOf(subOrderEntity))
        verify(paymentEventMapper).toPaymentEvent(eq(orderEntity), any(), any())

        assertEquals(1, result.size)
        assertSame(paymentEvent, result.first())
    }

    @Test
    fun `должен вернуть пустой список, если входная пачка пуста`() {
        val result = service.upsertBatch(emptyList())
        verify(orderDao, never()).upsertOrders(any())
        verify(subOrderDao, never()).upsertSubOrders(any())
        verify(paymentEventMapper, never()).toPaymentEvent(any(), any(), any())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `должен пропустить дубликат orderId`() {
        val duplicate =
            dto.copy(
                managerEmail = "duplicate@mail.com",
                subOrders =
                    listOf(
                        dto.subOrders.first().copy(policyId = "anotherPolicy"),
                    ),
            )
        val result = service.upsertBatch(listOf(dto, duplicate))
        verify(orderMapper, times(1)).toOrderEntity(any())
        verify(paymentEventMapper, times(1))
            .toPaymentEvent(eq(orderEntity), any(), any())

        assertEquals(1, result.size)
    }

    @Test
    fun `должен выбросить исключение, если orderDao завершился ошибкой`() {
        whenever(orderDao.upsertOrders(any())).thenThrow(RuntimeException("DB error"))

        val exception =
            assertThrows<RuntimeException> {
                service.upsertBatch(listOf(dto))
            }

        assertEquals("DB error", exception.message)
        verify(orderDao).upsertOrders(any())
        verify(subOrderDao, never()).upsertSubOrders(any())
        verify(paymentEventMapper, never()).toPaymentEvent(any(), any(), any())
    }
}
