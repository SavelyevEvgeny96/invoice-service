package ru.sogaz.site.orderingService.service.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.PaymentEventProducer

class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val messageConverter: MessageConverter,
) : OrderBatchConsumer {
    companion object {
        private const val SUCCESSFUL_QUEUE_PROCESSING =
            "Обработка записи из очереди успешно произведена: routingKey=%s, author=%s, eventTime=%s"
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
    }

    private val logger = loggerFor(javaClass)

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

        // парсим сообщения
        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            try {
                val dto = messageConverter.fromMessage(msg) as OrderPayloadDto
                payloads += tag to dto
            } catch (ex: Exception) {
                logger.error("Ошибка парсинга сообщения: ${msg.messageProperties.messageId}")
                // отправляем битое сообщение в DLQ
                channel.basicReject(tag, false)
            }
        }

        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        try {
            // 1) запись в БД + сбор событий
            val events = buildBatchConsumerService.upsertBatch(payloads.map { it.second })

            // 2) публикация событий
            paymentProducer.sendBatch(events)

            // 3) ack только за валидные сообщения
            payloads.forEach { (tag, _) ->
                channel.basicAck(tag, false)
            }

            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}")
            // если ошибка на уровне БД/бизнес-логики → отправляем валидные в DLQ тоже
            payloads.forEach { (tag, _) ->
                channel.basicReject(tag, false)
            }
        }
    }
}
