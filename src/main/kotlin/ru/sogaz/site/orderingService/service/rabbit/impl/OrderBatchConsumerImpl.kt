package ru.sogaz.site.orderingService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.Parsed
import ru.sogaz.site.orderingService.dto.data.RefundErrorDto
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.rabbit.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.PaymentEventProducer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val objectMapper: ObjectMapper,
    private val rabbitProps: RabbitProps,
    private val sendMessageProducer: SendMessageProducer
) : OrderBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED =
            "Нет валидных сообщений для обработки" +
                    " в батче по созданию заказа"
        private const val NOT_VALID_BATCH_MESSAGE_REFUND_ORDER =
            "Нет валидных сообщений для обработки " +
                    "в батче по возврату заказа "
        private const val ORDER_NOT_FOUND = "Номер счета не найден"
        private const val ERROR = "error"
    }

    private val logger = loggerFor(OrderBatchConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatchOrderCreated(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()
        val payloads = parseBatch(messages, channel, OrderPayloadDto::class.java)
        if (payloads.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            return
        }
        try {
            val events = buildBatchConsumerService.insertBatchOrderCreated(payloads.map { it.dto })
            if (events.isNotEmpty()) {
                paymentProducer.sendBatchOrderCreated(events)
                payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }

                val tookMs = (System.nanoTime() - started) / 1_000_000
                logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
            }
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}", ex)
            payloads.forEach { (tag, _) -> channel.basicReject(tag, false) }
        }
    }

    @RabbitListener(
        queues = ["\${app.rabbit.queue-order-refund}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatchRefundCreated(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()

        val parsed = parseBatch(messages, channel, RefundPayloadDto::class.java)
        if (parsed.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_REFUND_ORDER)
            return
        }

        try {
            val resultOrder = buildBatchConsumerService.searchAndPreparationOrder(parsed)
            val missing = resultOrder.missing
            val found = resultOrder.found
            if (missing.isNotEmpty()) {
                missing.forEach { miss ->
                    val errorRefund = miss.dto
                    val rk = errorRefund.routingKey ?: ""
                    val errorDto = RefundErrorDto(
                        errorRefund.metaInfo,
                        errorRefund.orderId,
                        ERROR,
                        ORDER_NOT_FOUND,
                    )
                    sendMessageProducer.sendMessage(rk, errorDto, rabbitProps.ordersExchange, errorRefund.orderId)
                    // ack только после успешной отправки
                    channel.basicAck(miss.tag, false)
                }
            }

            // 5) Для found:
            if (found.isNotEmpty()) {
                found.forEach { f ->
                    val errorRefund = f.dto
                    val rk = errorRefund.routingKey ?: ""
                    val errorDto = RefundErrorDto(
                        errorRefund.metaInfo,
                        errorRefund.orderId,
                        ERROR,
                        ORDER_NOT_FOUND,
                    )
                    sendMessageProducer.sendMessage(rk, errorDto, rabbitProps.ordersExchange, errorRefund.orderId)
                    // ack только после успешной отправки
                    channel.basicAck(f.tag, false)
                }
            }

            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(parsed.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)
            parsed.forEach { channel.basicReject(it.tag, false) }
        }
    }

    private fun <T : Any> parseBatch(
        messages: List<Message>,
        channel: Channel,
        dtoClass: Class<T>,
    ): List<Parsed<T>> {
        val result = mutableListOf<Parsed<T>>()

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            val messageId = msg.messageProperties.messageId
            try {
                val body = String(msg.body, Charsets.UTF_8)
                val dto = objectMapper.readValue(body, dtoClass)
                result += Parsed(tag = tag, dto = dto, messageId = messageId)
            } catch (ex: Exception) {
                logger.error("Ошибка парсинга сообщения: $messageId (tag=$tag)", ex)
                channel.basicReject(tag, false)
            }
        }

        return result
    }
}
