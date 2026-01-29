package ru.sogaz.site.orderingService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message

interface OrderRefundBatchConsumer {
    fun handleBatchRefundCreated(
        messages: List<Message>,
        channel: Channel,
    )
}
