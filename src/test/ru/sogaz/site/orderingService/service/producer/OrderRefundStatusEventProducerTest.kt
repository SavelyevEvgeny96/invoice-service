package ru.sogaz.site.orderingService.service.producer

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import ru.sogaz.site.orderingService.mappers.order.OrderRefundMapper
import ru.sogaz.site.orderingService.mappers.order.OrderRefundMapperImpl
import ru.sogaz.site.orderingService.producer.OrderRefundStatusEventProducer
import ru.sogaz.site.orderingService.producer.OrderRefundStatusEventProducerImpl
import ru.sogaz.site.orderingService.properties.RabbitProps

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [OrderRefundMapperImpl::class])
class OrderRefundStatusEventProducerTest {
    @RelaxedMockK
    private lateinit var rabbitTemplate: RabbitTemplate

    @RelaxedMockK
    private lateinit var rabbitProps: RabbitProps

    @Autowired
    private lateinit var orderRefundMapper: OrderRefundMapper

    private lateinit var producer: OrderRefundStatusEventProducer

    @RelaxedMockK
    private lateinit var order: OrderEntity

    @RelaxedMockK
    private lateinit var completedPaymentData: CompletedPaymentData

    @BeforeEach
    fun beforeEach() {
        producer =
            OrderRefundStatusEventProducerImpl(
                rabbitTemplate,
                rabbitProps,
                orderRefundMapper,
            )
    }

    @Test
    fun `producer should send order info`() {
        every { completedPaymentData.status } returns PaymentOperationStateEnum.SUCCESS

        val refundMessage = getCapturedTestMessage()

        assertThat(refundMessage)
            .returns(order.orderId, RefundResponseDto::invoiceId)
            .returns(completedPaymentData.status.toString(), RefundResponseDto::status)
            .returns(completedPaymentData.errorText, RefundResponseDto::errorText)
    }

    @Test
    fun `producer should throw an exception when clientId is null`() {
        every { order.clientId } returns null

        assertThrows<IllegalArgumentException> { getCapturedTestMessage() }
    }

    private fun getCapturedTestMessage(): RefundResponseDto {
        val messageSlot = slot<RefundResponseDto>()

        producer.sendRefundStatus(order, completedPaymentData)

        verify {
            rabbitTemplate.convertAndSend(
                rabbitProps.ordersExchange,
                any(),
                capture(messageSlot),
                any(),
                any<CorrelationData>(),
            )
        }

        return messageSlot.captured
    }
}
