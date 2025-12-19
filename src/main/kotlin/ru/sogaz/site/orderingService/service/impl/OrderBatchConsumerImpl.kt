package ru.sogaz.site.orderingService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.orderingService.converters.NoOpMessageConverter
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.Parsed
import ru.sogaz.site.orderingService.dto.data.RefundErrorDto
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
    override fun handleBatchRefundCreated(messages: List<Message>, channel: Channel) {
        val started = System.nanoTime()

        val parsed = parseBatch(messages, channel, RefundPayloadDto::class.java)
        if (parsed.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_REFUND_ORDER)
            return
        }

        try {
            // 1) Собрали UUID
            val orderIds = parsed.asSequence().map { it.dto.orderId }.distinct().toList()

            // 2) Достали ордера и сделали map для быстрых lookup
            val ordersById = orderDao.findByIds(orderIds).associateBy { it.orderId }

            // 3) Разделили сообщения: найден / не найден
            val (found, missing) = parsed.partition { ordersById.containsKey(it.dto.orderId) }

            // 4) Для missing: отправили в очередь ошибок, и только ПОСЛЕ успеха — ack их тегов
            if (missing.isNotEmpty()) {
                val errorDtos = missing.map { p ->
                    RefundErrorDto( // пример
                        p.dto.metaInfo,
                        p.dto.orderId,
                        "error",
                        ""

                    )
                }

                errorProducer.sendBatch(errorDtos)   // или цикл send(...)
                missing.forEach { channel.basicAck(it.tag, false) }
            }

            // 5) Для found: твоя бизнес-логика + send + ack
            if (found.isNotEmpty()) {
                val inputs = found.map { p ->
                    val order = ordersById.getValue(p.dto.orderId)
                    order to p.dto
                }

                val events = buildBatchConsumerService.handleRefunds(inputs) // пример
                if (events.isNotEmpty()) {
                    paymentProducer.sendBatch(events)
                }

                found.forEach { channel.basicAck(it.tag, false) }
            }

            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(parsed.size, tookMs))

        } catch (ex: Exception) {
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)
            // если упали — решай стратегию:
            // 1) reject(false) чтобы в DLQ, или
            // 2) reject(true) чтобы ре-queue (но осторожно с бесконечными ретраями)
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
