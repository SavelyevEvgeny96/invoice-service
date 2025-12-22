package ru.sogaz.site.orderingService.service.rabbit

import java.util.UUID

interface SendMessageProducer {
    fun sendMessage(
        routingKey: String,
        paidOrderMessage: Any,
        exchange: String,
        orderId: UUID?
    )
}
