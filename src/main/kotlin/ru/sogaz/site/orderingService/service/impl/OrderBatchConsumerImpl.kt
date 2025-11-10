package ru.sogaz.site.orderingService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.orderingService.converters.NoOpMessageConverter
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.PaymentEventProducer

class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val messageConverter: NoOpMessageConverter,
    private val objectMapper: ObjectMapper,
) : OrderBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
    }

    private val logger = loggerFor(OrderBatchConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()
        val payloads = mutableListOf<Pair<Long, OrderPayloadDto>>() // tag + dto

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            try {
                val body = String(msg.body, Charsets.UTF_8)
                val dto = objectMapper.readValue(body, OrderPayloadDto::class.java)
                payloads += tag to dto
            } catch (ex: Exception) {
                logger.error("Ошибка парсинга сообщения: ${msg.messageProperties.messageId} (tag=$tag)", ex)
                channel.basicReject(tag, false)
            }
        }
        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }
        try {
            val events = buildBatchConsumerService.upsertBatch(payloads.map { it.second })
            paymentProducer.sendBatch(events)
            payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }

            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}", ex)
            payloads.forEach { (tag, _) -> channel.basicReject(tag, false) }
        }
    }
}
