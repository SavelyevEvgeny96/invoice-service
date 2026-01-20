import com.rabbitmq.client.Channel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.data.ParsedData
import ru.sogaz.site.orderingService.dto.data.RefundErrorDto
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.enums.RefundErrorReason
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.impl.SendMessageProducerImpl
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SendMessageProducerImplTest {
    @Mock
    lateinit var rabbitTemplate: RabbitTemplate

    @Mock
    lateinit var rabbitProps: RabbitProps

    @Mock
    lateinit var refundErrorMapper: RefundErrorMapper

    @Mock
    lateinit var channel: Channel

    @InjectMocks
    lateinit var producer: SendMessageProducerImpl

    @Test
    fun `когда все группы пустые - ничего не отправляем и не подтверждаем`() {
        val result =
            RefundPreparationResult(
                found = emptyList(),
                missing = emptyList(),
                noAccess = emptyList(),
                notForPaid = emptyList(),
            )

        producer.sendMessageRefund(result, channel)

        verifyNoInteractions(rabbitTemplate)
        verifyNoInteractions(refundErrorMapper)
        verify(channel, never()).basicAck(any(), any())
    }

    @Test
    fun `когда есть missing notForPaid noAccess found - отправляем нужные сообщения и подтверждаем каждое`() {
        whenever(rabbitProps.ordersExchange).thenReturn("orders-exchange")

        val payloadMissing =
            RefundPayloadDto(
                orderId = UUID.randomUUID(),
                routingKeyStatus = "rk.missing",
                metaInfo = emptyList()
            )

        val payloadNotPaid =
            RefundPayloadDto(
                orderId = UUID.randomUUID(),
                routingKeyStatus = "rk.notPaid",
                metaInfo = emptyList()
            )

        val payloadNoAccess =
            RefundPayloadDto(
                orderId = UUID.randomUUID(),
                routingKeyStatus = "rk.noAccess",
                metaInfo = emptyList()
            )

        val payloadFound =
            RefundPayloadDto(
                orderId = UUID.randomUUID(),
                routingKeyStatus = "rk.found",
                metaInfo = emptyList()
            )

        val miss = ParsedData(tag = 11L, dto = payloadMissing, messageId = "m1")
        val notPaid = ParsedData(tag = 22L, dto = payloadNotPaid, messageId = "m2")
        val noAcc = ParsedData(tag = 33L, dto = payloadNoAccess, messageId = "m3")
        val found = ParsedData(tag = 44L, dto = payloadFound, messageId = "m4")

        val result =
            RefundPreparationResult(
                missing = listOf(miss),
                notForPaid = listOf(notPaid),
                noAccess = listOf(noAcc),
                found = listOf(found),
            )

        val errorMissing = org.mockito.kotlin.mock<RefundErrorDto>()
        val errorNotPaid = org.mockito.kotlin.mock<RefundErrorDto>()
        val errorNoAccess = org.mockito.kotlin.mock<RefundErrorDto>()

        whenever(refundErrorMapper.toErrorDto(payloadMissing, RefundErrorReason.ORDER_NOT_FOUND))
            .thenReturn(errorMissing)
        whenever(refundErrorMapper.toErrorDto(payloadNotPaid, RefundErrorReason.NOT_PAID_FOR))
            .thenReturn(errorNotPaid)
        whenever(refundErrorMapper.toErrorDto(payloadNoAccess, RefundErrorReason.NO_ACCESS))
            .thenReturn(errorNoAccess)
        whenever(rabbitProps.routingKeyRefundPayment).thenReturn("rk.refund.success")
        whenever(rabbitProps.paymentsExchange).thenReturn("payments-exchange")
        producer.sendMessageRefund(result, channel)

        verify(rabbitTemplate).convertAndSend(
            eq("orders-exchange"),
            eq("rk.missing"),
            eq(errorMissing),
            any<MessagePostProcessor>(),
            any<CorrelationData>(),
        )

        verify(rabbitTemplate).convertAndSend(
            eq("orders-exchange"),
            eq("rk.notPaid"),
            eq(errorNotPaid),
            any<MessagePostProcessor>(),
            any<CorrelationData>(),
        )

        verify(rabbitTemplate).convertAndSend(
            eq("orders-exchange"),
            eq("rk.noAccess"),
            eq(errorNoAccess),
            any<MessagePostProcessor>(),
            any<CorrelationData>(),
        )
        verify(channel).basicAck(11L, false)
        verify(channel).basicAck(22L, false)
        verify(channel).basicAck(33L, false)
        verify(channel).basicAck(44L, false)

        verify(rabbitTemplate, times(4)).convertAndSend(
            any<String>(),
            any<String>(),
            any<Any>(),
            any<MessagePostProcessor>(),
            any<CorrelationData>(),
        )
    }

    @Test
    fun `sendMessage проставляет заголовки и correlation data`() {
        val orderId = UUID.randomUUID()
        val payload = Any()

        producer.sendMessage(
            routingKey = "rk.test",
            payload = payload,
            exchange = "ex.test",
            orderId = orderId,
        )

        val mppCaptor = argumentCaptor<MessagePostProcessor>()
        val cdCaptor = argumentCaptor<CorrelationData>()

        verify(rabbitTemplate).convertAndSend(
            eq("ex.test"),
            eq("rk.test"),
            eq(payload),
            mppCaptor.capture(),
            cdCaptor.capture(),
        )

        assertEquals(orderId.toString(), cdCaptor.firstValue.id)

        val message = Message(ByteArray(0), MessageProperties())
        val processed = mppCaptor.firstValue.postProcessMessage(message)
        val headers = processed.messageProperties.headers

        assertEquals("payService", headers["author"])
        assertEquals("ResultPay", headers["flowCode"])

        val ts = headers["timestamp"] as String?
        assertNotNull(ts)
        assertNotNull(OffsetDateTime.parse(ts))

        assertEquals("ex.test", headers[RabbitLogConst.HDR_X_EXCHANGE])
        assertEquals("rk.test", headers[RabbitLogConst.HDR_X_ROUTINGKEY])
        assertEquals(orderId.toString(), processed.messageProperties.correlationId)
    }
}
