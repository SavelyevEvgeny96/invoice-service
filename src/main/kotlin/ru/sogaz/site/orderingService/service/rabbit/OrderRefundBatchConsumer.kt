package ru.sogaz.site.orderingService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.messaging.handler.annotation.Payload
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto

interface OrderRefundBatchConsumer {
    fun handleBatchRefundCreated(
        @Payload refundEvent: RefundPayloadDto
    )

}
