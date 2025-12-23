package ru.sogaz.site.orderingService.service.rabbit

import com.rabbitmq.client.Channel
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import java.util.UUID

interface SendMessageProducer {
    fun sendMessageRefund(
        resultOrder: RefundPreparationResult,
        channel: Channel
    )
}
