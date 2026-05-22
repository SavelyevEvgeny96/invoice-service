package ru.sogaz.site.orderingService.service.rabbit

import org.springframework.amqp.core.Message

interface OrderRefundBatchConsumer {
    fun handleBatchRefundCreated(message: Message)
}
