package ru.sogaz.site.orderingService.service.receipt.impl

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.receipt.ReceiptClient
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest

@Component
@Transactional(rollbackFor = [Exception::class])
class ReceiptClientImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProps,
) : ReceiptClient {
    override fun sendReceiptToQueue(receiptCreateRequest: PaymentReceiptCreateRequest) {
        rabbitTemplate.convertAndSend(
            rabbitProperties.receiptExchange,
            rabbitProperties.routingKeyReceipt,
            receiptCreateRequest,
            CorrelationData(receiptCreateRequest.orderId.toString()),
        )
    }
}
