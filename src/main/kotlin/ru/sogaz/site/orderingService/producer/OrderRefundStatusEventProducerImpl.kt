package ru.sogaz.site.orderingService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.order.OrderRefundMapper
import ru.sogaz.site.orderingService.properties.RabbitProps

@Component
class OrderRefundStatusEventProducerImpl(
    rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val orderRefundMapper: OrderRefundMapper,
) : RabbitProducer<RefundResponseDto>(rabbitTemplate),
    OrderRefundStatusEventProducer {
    companion object {
        private val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
        private const val REFUND_ROUTING_KEY_PATTERN = "order.status.refund.%s.created"
    }

    override fun sendRefundStatus(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) = convertAndSend(
        rabbitProps.ordersExchange,
        requireNotNull(order.buildRefundRoutingKey()),
        orderRefundMapper.toRefundResponseDto(order, completedPaymentData),
        order.orderId,
    )

    private fun OrderEntity.buildRefundRoutingKey() =
        clientId
            ?.replace(NON_ALPHANUMERIC_REGEX, ".")
            ?.let { REFUND_ROUTING_KEY_PATTERN.format(it) }
}
