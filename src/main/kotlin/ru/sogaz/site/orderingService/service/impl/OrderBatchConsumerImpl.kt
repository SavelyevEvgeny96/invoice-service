package ru.sogaz.site.orderingService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.orderingService.converters.NoOpMessageConverter
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.PaymentEventProducer

class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val objectMapper: ObjectMapper,
    private val orderDao: OrderDao
) : OrderBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED = "Нет валидных сообщений для обработки" +
                " в батче по созданию заказа"
        private const val NOT_VALID_BATCH_MESSAGE_REFUND_ORDER = "Нет валидных сообщений для обработки " +
                "в батче по возврату заказа "
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
            val events = buildBatchConsumerService.insertBatchOrderCreated(payloads.map { it.second })
            if (events.isNotEmpty()) {
                paymentProducer.sendBatch(events)
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
        val payloads = parseBatch(messages, channel, RefundPayloadDto::class.java)
        if (payloads.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_REFUND_ORDER)
            return
        }
        try {
        val listOrderEntity = orderDao.findByIds()

            if (events.isNotEmpty()) {
                paymentProducer.sendBatch(events)
                payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }

                val tookMs = (System.nanoTime() - started) / 1_000_000
                logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
            }
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}", ex)
            payloads.forEach { (tag, _) -> channel.basicReject(tag, false) }
        }
    }

    private fun <T : Any> parseBatch(
        messages: List<Message>,
        channel: Channel,
        dtoClass: Class<T>,
    ): List<Pair<Long, T>> {
        val payloads = mutableListOf<Pair<Long, T>>()

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            try {
                val body = String(msg.body, Charsets.UTF_8)
                val dto = objectMapper.readValue(body, dtoClass)
                payloads += tag to dto
            } catch (ex: Exception) {
                logger.error(
                    "Ошибка парсинга сообщения: ${msg.messageProperties.messageId} (tag=$tag)",
                    ex
                )
                channel.basicReject(tag, false)
            }
        }

        return payloads
    }
}
