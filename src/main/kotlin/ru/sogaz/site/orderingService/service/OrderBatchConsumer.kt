package ru.sogaz.site.orderingService.service

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message

interface OrderBatchConsumer {
    fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    )
}
