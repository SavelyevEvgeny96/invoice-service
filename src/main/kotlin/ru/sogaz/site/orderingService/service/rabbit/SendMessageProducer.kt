package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import java.util.UUID

interface SendMessageProducer {
    fun sendMessageRefund(resultOrder: RefundPreparationResult)

    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: UUID?,
    )
}
