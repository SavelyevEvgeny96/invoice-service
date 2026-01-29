import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
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
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.rabbit.impl.SendMessageProducerImpl
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SendMessageProducerImplTest {
    @Mock
    lateinit var queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService

    @Mock
    lateinit var rabbitTemplate: RabbitTemplate

    @Mock
    lateinit var rabbitProps: RabbitProps

    @Mock
    lateinit var refundErrorMapper: RefundErrorMapper

    @Mock
    lateinit var channel: Channel

    @Mock
    lateinit var objectMapper: ObjectMapper

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

        producer.sendMessageRefund(result)

        verifyNoInteractions(rabbitTemplate)
        verifyNoInteractions(refundErrorMapper)
        verify(channel, never()).basicAck(any(), any())
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

    @Test
    fun `если брокер не подтвердил сообщение то выбрасывается исключение`() {
        // given
        val channel = mock(Channel::class.java)

        whenever(channel.waitForConfirms(3000)).thenReturn(false)

        // when + then
        assertThrows<RuntimeException> {
            producer.sendRawMessageWithConfirm(
                channel = channel,
                exchange = "ex",
                routingKey = "rk",
                rawBody = "body",
            )
        }

        verify(channel).confirmSelect()
        verify(channel).waitForConfirms(3000)
    }

    @Test
    fun `битое сообщение переотправляется и подтверждается ack`() {
        // given
        val channel = mock(Channel::class.java)
        val exchange = "test-exchange"
        val statusPattern = "status.%s.%s"
        val routingKey = "normalized.key"
        val errorParsed =
            ParsedResult.Error<Any>(
                author = "ordering-client",
                rawMessage = """{"bad":"json"}""",
                tag = 42L,
                messageId = "1",
            )

        whenever(
            queueStatusResultNameNormalizeService.buildQueueStatusResultName(
                statusPattern,
                errorParsed.author,
            ),
        ).thenReturn(routingKey)

        whenever(channel.waitForConfirms(3000)).thenReturn(true)

        // when
        producer.processErrorMessages(
            errorParsed = errorParsed,
            channel = channel,
            exchange = exchange,
            statusPattern = statusPattern,
        )

        // then
        verify(channel).basicPublish(
            eq(exchange),
            eq(routingKey),
            argThat {
                contentType == "application/json" &&
                    deliveryMode == 2
            },
            eq(errorParsed.rawMessage.toByteArray(Charsets.UTF_8)),
        )

        verify(channel).basicAck(errorParsed.tag, false)
    }

    @Test
    fun `сообщение публикуется и подтверждается брокером`() {
        // given
        val channel = mock(Channel::class.java)
        val exchange = "test-exchange"
        val routingKey = "test.routing"
        val rawBody = """{"test":"ok"}"""

        whenever(channel.waitForConfirms(3000)).thenReturn(true)

        // when
        producer.sendRawMessageWithConfirm(
            channel = channel,
            exchange = exchange,
            routingKey = routingKey,
            rawBody = rawBody,
        )

        verify(channel).basicPublish(
            eq(exchange),
            eq(routingKey),
            argThat {
                contentType == "application/json" &&
                    deliveryMode == 2
            },
            eq(rawBody.toByteArray(Charsets.UTF_8)),
        )
        verify(channel).waitForConfirms(3000)
    }
}
