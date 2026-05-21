package ru.sogaz.site.orderingService.service.producer

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.PaidOrderMessage
import ru.sogaz.site.orderingService.dto.data.SubOrderPayload
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import ru.sogaz.site.orderingService.mappers.order.PaidOrderMessagesMapper
import ru.sogaz.site.orderingService.mappers.order.PaidOrderMessagesMapperImpl
import ru.sogaz.site.orderingService.producer.InvoicePaymentStatusRegEventProducer
import ru.sogaz.site.orderingService.producer.OrderPaymentStatusEventProducerImpl
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.time.Instant

@ExtendWith(MockKExtension::class)
class OrderPaymentStatusEventProducerTest {
    @RelaxedMockK
    private lateinit var rabbitTemplate: RabbitTemplate

    @RelaxedMockK
    private lateinit var rabbitProps: RabbitProps

    private lateinit var paidOrderMessagesMapper: PaidOrderMessagesMapper

    private lateinit var producer: OrderPaymentStatusEventProducerImpl

    @RelaxedMockK
    private lateinit var sendMessageProducer: SendMessageProducer

    @RelaxedMockK
    private lateinit var invoicePaymentStatusRegEventProducer: InvoicePaymentStatusRegEventProducer

    @RelaxedMockK
    private lateinit var order: OrderEntity

    @RelaxedMockK
    private lateinit var subOrder: SubOrderEntity

    @RelaxedMockK
    private lateinit var completedPaymentData: CompletedPaymentData

    @BeforeEach
    fun beforeEach() {
        paidOrderMessagesMapper = PaidOrderMessagesMapperImpl()

        every { rabbitProps.ordersExchange } returns "orders.exchange"
        every { order.queueStatusResultName } returns "queue.status.result"

        producer =
            OrderPaymentStatusEventProducerImpl(
                rabbitTemplate,
                rabbitProps,
                paidOrderMessagesMapper,
                invoicePaymentStatusRegEventProducer,
                sendMessageProducer,
            )
    }

    @Test
    fun `producer should not fail while sending payment order event`() {
        assertThatCode {
            producer.sendPaymentOrderEvent(order, completedPaymentData)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `producer should send payment and card info`() {
        val payDate = Instant.now()

        with(completedPaymentData) {
            every { this@with.payDate } returns payDate
            every { status } returns PaymentOperationStateEnum.SUCCESS

            val paidOrderMessage = getMappedTestMessage()

            assertThat(paidOrderMessage)
                .returns(paymentType, PaidOrderMessage::paymentType)
                .returns(bank, PaidOrderMessage::bank)
                .returns(payDate) { Instant.parse(it.paySuccess.toString()) }
                .returns(card?.maskedPan, PaidOrderMessage::maskedPan)
                .returns(card?.paymentSystem, PaidOrderMessage::paymentSystem)
                .returns(card?.cardId, PaidOrderMessage::keyCard)
                .returns(card?.issuerName, PaidOrderMessage::issuerName)
                .returns(status.toString(), PaidOrderMessage::status)
        }
    }

    @Test
    fun `producer should send order info`() {
        with(order) {
            val paidOrderMessage = getMappedTestMessage()

            assertThat(paidOrderMessage)
                .returns(orderId.toString(), PaidOrderMessage::orderId)
                .returns(recipientEmail, PaidOrderMessage::recipientEmail)
                .returns(clientId, PaidOrderMessage::externalSystemCode)
                .returns(subscriptionId, PaidOrderMessage::subscriptionId)
        }
    }

    @Test
    fun `producer should correct send subOrders contract info`() {
        every { order.subOrders } returns mutableListOf(subOrder)

        with(subOrder) {
            val paidOrderMessage = getMappedTestMessage()

            assertThat(paidOrderMessage.subOrders.first())
                .returns(docType, SubOrderPayload::docType)
                .returns(policyId, SubOrderPayload::policyId)
                .returns(policyNumber, SubOrderPayload::policyNumber)
                .returns(contractNumber, SubOrderPayload::contractNumber)
                .returns(contractId, SubOrderPayload::contractId)
                .returns(insuranceProgram, SubOrderPayload::typeInsurance)
                .returns(premiumAmount.toString(), SubOrderPayload::premiumAmount)
                .returns(channel, SubOrderPayload::channel)
        }
    }

    @Test
    fun `producer should correct send correct subOrders contract dates`() {
        val now = Instant.now()

        every { order.subOrders } returns mutableListOf(subOrder)
        every { subOrder.contractDate } returns now
        every { subOrder.policyDate } returns now

        val paidOrderMessage = getMappedTestMessage()

        assertThat(paidOrderMessage.subOrders.first())
            .returns(now.toEpochMilli(), SubOrderPayload::policyDate)
            .returns(now.toEpochMilli(), SubOrderPayload::contractDate)
    }

    private fun getMappedTestMessage(): PaidOrderMessage = paidOrderMessagesMapper.toPaidOrderMessage(order, completedPaymentData)
}
