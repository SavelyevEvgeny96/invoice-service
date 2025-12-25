package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.orderingService.dto.data.RefundErrorDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.enums.RefundErrorReason
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.RefundErrorMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProps: RabbitProps,
    private val refundErrorMapper: RefundErrorMapper
) : SendMessageProducer {
    private val logger = loggerFor(SendMessageProducerImpl::class.java)

    companion object {
        private const val ORDER_NOT_FOUND = "Номер счета не найден"
        private const val ERROR = "error"
        const val START_LOG_MESSAGE_QUEUE = "Старт записи в очередь routingKey: %s  exchange: %s "
    }

    override fun sendMessageRefund(resultOrder: RefundPreparationResult, channel: Channel) {
        val missing = resultOrder.missing
        val found = resultOrder.found
        val noAccess = resultOrder.noAccess
        if (missing.isNotEmpty()) {
            missing.forEach { miss ->
                val payload = miss.dto
                val rk = payload.routingKeyStatus.orEmpty()
                val errorDto = refundErrorMapper.toErrorDto(payload, RefundErrorReason.ORDER_NOT_FOUND)
                sendMessage(rk, errorDto, rabbitProps.ordersExchange, payload.orderId)
                channel.basicAck(miss.tag, false)
            }
        }

        if (noAccess.isNotEmpty()) {
            noAccess.forEach { noAcc ->
                val payload = noAcc.dto
                val rk = payload.routingKeyStatus.orEmpty()
                val errorDto = refundErrorMapper.toErrorDto(payload, RefundErrorReason.NO_ACCESS)
                sendMessage(rk, errorDto, rabbitProps.ordersExchange, payload.orderId)
                channel.basicAck(noAcc.tag, false)
            }
        }
        // 5) Для found:
        if (found.isNotEmpty()) {
            found.forEach { f ->
                val refund = f.dto
                val rk = refund.routingKeyStatus ?: ""
                val errorDto =
                    RefundErrorDto(
                        refund.metaInfo,
                        refund.orderId,
                        ERROR,
                        ORDER_NOT_FOUND,
                    )
                sendMessage(rk, errorDto, rabbitProps.ordersExchange, refund.orderId)
                // ack только после успешной отправки
                channel.basicAck(f.tag, false)
            }
        }

    }

    override fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: UUID?,
    ) {
        val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val cd = CorrelationData(orderId?.toString() ?: UUID.randomUUID().toString())

        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            payload,
            { message ->
                message.messageProperties.headers["author"] = "payService"
                message.messageProperties.headers["flowCode"] = "ResultPay"
                message.messageProperties.headers["timestamp"] = timestamp
                message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                message.messageProperties.correlationId = orderId?.toString()
                message
            },
            cd
        )
    }

}
