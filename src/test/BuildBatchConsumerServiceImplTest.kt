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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import ru.sogaz.site.orderingService.dao.ClientSystemDao
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
import ru.sogaz.site.orderingService.service.rabbit.impl.BuildBatchConsumerServiceImpl
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
    lateinit var clientSystemDao: ClientSystemDao

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
                managerEmail = "manager@mail.",
            )

        dto =
            OrderPayloadDto(
                metaInfo = emptyList(),
                recipientEmail = "client@mail.com",
                recipientPhone = "+79998887766",
                recipientUserId = "user123",
                unifiedId = "gd999",
                keyCard = null,
                orderEndDate = Instant.now(),
                subOrders = listOf(subOrderDto),
                subscriptionId = "subscriptionId",
                bank = "gpb",
                paymentType = "card",
                policyholder = "Gena Vanya",
            )

        orderEntity =
            OrderEntity(
                orderId = UUID.randomUUID(),
                recipientEmail = dto.recipientEmail ?: "",
                recipientPhone = dto.recipientPhone ?: "",
                premiumAmount = BigDecimal.TEN,
                paymentEndDate = dto.orderEndDate,
                updateDate = Instant.now(),
                keyCard = dto.keyCard,
                recipientUserId = dto.recipientUserId,
                unifiedId = dto.unifiedId,
                bank = null,
                policyholder = dto.policyholder,
                paymentType = "",
                recurrent = false,
                saveCard = true,
                subscriptionId = "",
                createDate = Instant.now(),
                urlToDecline = "",
                urlToReturn = "",
                refundDate = Instant.now(),
                queueStatusResultName = "",
                depersonalization = false,
                urlPayPageShort = null,
                typePaymentOperation = null,
                versionApi = null,
            )

        subOrderEntity =
            SubOrderEntity(
                id = UUID.randomUUID(),
                orderEntity = orderEntity,
                policyId = subOrderDto.policyId,
                policyNumber = subOrderDto.policyNumber,
                contractId = subOrderDto.contractId,
                contractNumber = subOrderDto.contractNumber,
                insuranceProgram = subOrderDto.insuranceProgram,
                typeInsurance = subOrderDto.typeInsurance,
                premiumAmount = subOrderDto.premiumAmountDto,
                managerEmail = subOrderDto.managerEmail,
                docType = "",
                channel = "",
                mainContractCheck = true,
                createDate = Instant.now(),
                policyDate = Instant.now(),
                updateDate = Instant.now(),
                contractDate = Instant.now(),
                typeOperation = null,
            )

        paymentEvent =
            PaymentCreatedEvent(
                eventType = "type",
                timestamp = "timestamp",
                data =
                    PaymentData(
                        orderId = orderEntity.orderId!!,
                        premiumAmount = orderEntity.premiumAmount,
                        keyCard = orderEntity.keyCard,
                        recipientEmail = orderEntity.recipientEmail,
                        recipientPhone = orderEntity.recipientPhone,
                        dateCreate = orderEntity.updateDate?.toString(),
                        dateEnd = orderEntity.paymentEndDate?.toString(),
                        bank = orderEntity.bank.toString(),
                        paymentType = orderEntity.paymentType,
                    ),
            )

        // моки
        whenever(props.routingKeyPayment).thenReturn("type")
        whenever(orderMapper.toOrderEntity(dto)).thenReturn(orderEntity)
        whenever(orderMapper.toSubOrderEntity(any(), eq(orderEntity))).thenReturn(subOrderEntity)
        whenever(paymentEventMapper.toPaymentEvent(any(), any(), any())).thenReturn(paymentEvent)

        // ВАЖНО: новый метод — возвращаем map { subscriptionId -> orderId }
        whenever(orderDao.upsertOrdersReturningIds(any()))
            .thenReturn(listOf(orderEntity.orderId!!))
    }

    @Test
    fun `должен успешно обработать пачку заказов`() {
        val result = service.insertBatchOrderCreated(listOf(dto))

        verify(orderDao).upsertOrdersReturningIds(listOf(orderEntity))
        verify(subOrderDao).upsertSubOrders(listOf(subOrderEntity))
        assertEquals(1, result.size)
    }

    @Test
    fun `должен вернуть пустой список, если входная пачка пуста`() {
        val result = service.insertBatchOrderCreated(emptyList())

        verify(orderDao, never()).upsertOrdersReturningIds(any())
        verify(subOrderDao, never()).upsertSubOrders(any())
        verify(paymentEventMapper, never()).toPaymentEvent(any(), any(), any())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `должен выбросить исключение, если orderDao завершился ошибкой`() {
        whenever(orderDao.upsertOrdersReturningIds(any()))
            .thenThrow(RuntimeException("DB error"))

        val exception =
            assertThrows<RuntimeException> {
                service.insertBatchOrderCreated(listOf(dto))
            }

        assertEquals("DB error", exception.message)
        verify(orderDao).upsertOrdersReturningIds(any())
        verify(subOrderDao, never()).upsertSubOrders(any())
        verify(paymentEventMapper, never()).toPaymentEvent(any(), any(), any())
    }
}
