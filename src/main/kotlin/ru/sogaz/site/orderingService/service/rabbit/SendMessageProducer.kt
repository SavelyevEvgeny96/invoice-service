package ru.sogaz.site.orderingService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import java.util.UUID

interface SendMessageProducer {
    fun extractAuthorUnsafe(body: String): String?

    fun sendMessageRefund(resultOrder: RefundPreparationResult)

    fun <T : Any> parseBatch(
        messages: List<Message>,
        channel: Channel,
        dtoClass: Class<T>,
    ): List<ParsedResult<T>>

    fun <T : Any> processErrorMessages(
        errorParsed: ParsedResult.Error<T>,
        channel: Channel,
        exchange: String,
        statusPattern: String,
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
