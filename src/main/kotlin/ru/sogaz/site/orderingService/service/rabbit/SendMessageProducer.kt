package ru.sogaz.site.orderingService.service.rabbit

import com.rabbitmq.client.Channel
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import java.util.UUID

interface SendMessageProducer {
    fun sendMessageRefund(resultOrder: RefundPreparationResult)

    fun processErrorMessages(
        errorParsed: ParsedResult.Error<OrderPayloadDto>,
        channel: Channel,
        exchange: String,
    )

    fun sendRawMessageWithConfirm(
        channel: Channel,
        exchange: String,
        routingKey: String,
        rawBody: String,
    )

    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: UUID?,
    )
}
