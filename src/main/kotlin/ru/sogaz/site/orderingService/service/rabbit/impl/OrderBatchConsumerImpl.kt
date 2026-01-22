package ru.sogaz.site.orderingService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.PublishResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_REFUND_PATTERN
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.PAYMENT_STATUS_PATTERN
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.rabbit.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.PaymentEventProducer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Service
class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val objectMapper: ObjectMapper,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProps,
    private val queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService,
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
    }

    private val logger = loggerFor(OrderBatchConsumerImpl::class.java)

    /**
     * Обрабатывает batch сообщений из очереди заказов.
     *
     * <p>Метод получает список сообщений из RabbitMQ и делит их на два типа:
     * <ul>
     *     <li>{@link ParsedResult.Success} — валидные сообщения, которые удалось распарсить в DTO.</li>
     *     <li>{@link ParsedResult.Error} — битые сообщения, которые не соответствуют DTO,
     *         но из которых удалось извлечь поле "author".</li>
     * </ul>
     *
     * <p>Для валидных сообщений выполняется:
     * <ol>
     *     <li>Вставка данных в базу через {@link BuildBatchConsumerService#insertBatchOrderCreated}.</li>
     *     <li>Отправка событий в следующую очередь через {@link PaymentEventProducer#sendBatchOrderCreated}.</li>
     *     <li>ACK всех успешных сообщений одним вызовом {@link Channel#basicAck}, если все события подтверждены.</li>
     * </ol>
     *
     * <p>Для битых сообщений:
     * <ul>
     *     <li>Если найден author — логируем и передаём на внешнюю обработку (не DLQ).</li>
     *     <li>Если author не найден — parseBatch делает {@link Channel#basicReject}, сообщение попадёт в DLQ.</li>
     * </ul>
     *
     * <p>В случае исключения при обработке валидных сообщений:
     * <ul>
     *     <li>Все валидные сообщения возвращаются в очередь через {@link Channel#basicReject}.</li>
     *     <li>Битые сообщения с author не трогаются (можно отправлять на внешку).</li>
     * </ul>
     *
     * @param messages Список сообщений RabbitMQ для обработки.
     * @param channel Канал RabbitMQ, используемый для ACK/Reject сообщений.
     */
    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatchOrderCreated(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()

        // parseBatch теперь возвращает ParsedResult.Success и ParsedResult.Error
        val parsedResults = parseBatch(messages, channel, OrderPayloadDto::class.java)

        val successMessages = parsedResults.filterIsInstance<ParsedResult.Success<OrderPayloadDto>>()
        val errorMessages = parsedResults.filterIsInstance<ParsedResult.Error<OrderPayloadDto>>()

        // Если нет валидных сообщений — логируем и выходим
        if (successMessages.isEmpty() && errorMessages.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            return
        }

        try {
            // --- Обработка валидных сообщений ---
            if (successMessages.isNotEmpty()) {
                val dtos = successMessages.map { it.dto }
                val events = buildBatchConsumerService.insertBatchOrderCreated(dtos)
                if (events.isNotEmpty()) {
                    val result = paymentProducer.sendBatchOrderCreated(events)

                    // Один ACK на весь batch валидных сообщений
                    if (result.allConfirmed()) {
                        val lastTag = successMessages.last().tag
                        channel.basicAck(lastTag, true)
                    } else {
                        logger.warn("Не все сообщения подтверждены, batch не ACK")
                    }

                    val tookMs = (System.nanoTime() - started) / 1_000_000
                    logger.info(BATCH_SUMMARY.format(successMessages.size, tookMs))
                }
            }

            // --- Обработка битых сообщений с author ---
            errorMessages.forEach { err ->
                logger.warn("Битое сообщение от автора=${err.author}: ${err.rawMessage.take(100)}")
                val rKey =
                    queueStatusResultNameNormalizeService.buildQueueStatusResultName(PAYMENT_STATUS_PATTERN, err.author)
                sendMessageProducer.sendMessage(rKey, err.rawMessage, props.paymentsExchange, null)
            }
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}", ex)

            // Reject всех успешных сообщений, чтобы они вернулись в очередь
            successMessages.forEach { (tag, _) -> channel.basicReject(tag, false) }
        }
    }

    /**
     * Обрабатывает batch сообщений по возвратам заказов из очереди.
     *
     * <p>Метод получает список сообщений из RabbitMQ и делит их на два типа:
     * <ul>
     *     <li>{@link ParsedResult.Success} — валидные сообщения, которые удалось распарсить в DTO.</li>
     *     <li>{@link ParsedResult.Error} — битые сообщения, которые не соответствуют DTO,
     *         но из которых удалось извлечь поле "author".</li>
     * </ul>
     *
     * <p>Для валидных сообщений выполняется:
     * <ol>
     *     <li>Подготовка данных через {@link BuildBatchConsumerService#searchAndPreparationOrder}.</li>
     *     <li>Отправка сообщений через {@link SendMessageProducer#sendMessageRefund}.</li>
     *     <li>ACK всех валидных сообщений одним вызовом {@link Channel#basicAck} после успешной отправки.</li>
     * </ol>
     *
     * <p>Для битых сообщений:
     * <ul>
     *     <li>Если найден author — логируем и можно передать на внешку (пока не делаем DLQ).</li>
     *     <li>Если author не найден — реджектим сообщение через {@link Channel#basicReject} (DLQ).</li>
     * </ul>
     *
     * <p>В случае исключения при обработке валидных сообщений:
     * <ul>
     *     <li>Все валидные сообщения возвращаются в очередь через {@link Channel#basicReject}.</li>
     * </ul>
     *
     * @param messages Список сообщений RabbitMQ для обработки.
     * @param channel Канал RabbitMQ, используемый для ACK/Reject сообщений.
     */
    @RabbitListener(
        queues = ["\${app.rabbit.queue-order-refund}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatchRefundCreated(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()

        // parseBatch теперь возвращает ParsedResult.Success и ParsedResult.Error
        val parsedResults = parseBatch(messages, channel, RefundPayloadDto::class.java)

        val successMessages = parsedResults.filterIsInstance<ParsedResult.Success<RefundPayloadDto>>()
        val errorMessages = parsedResults.filterIsInstance<ParsedResult.Error<RefundPayloadDto>>()

        // Если нет валидных сообщений — логируем и выходим
        if (successMessages.isEmpty() && errorMessages.isEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_REFUND_ORDER)
            return
        }

        try {
            // --- Обработка валидных сообщений ---
            if (successMessages.isNotEmpty()) {
                val dtos = successMessages.map { it.dto }
                sendMessageProducer.sendMessageRefund(buildBatchConsumerService.searchAndPreparationOrder(dtos))

                // ACK всех успешных сообщений одним вызовом
                val lastTag = successMessages.last().tag
                channel.basicAck(lastTag, true)

                val tookMs = (System.nanoTime() - started) / 1_000_000
                logger.info(BATCH_SUMMARY.format(successMessages.size, tookMs))
            }

            // --- Обработка битых сообщений с author ---
            errorMessages.forEach { err ->
                logger.warn("Битое сообщение от автора=${err.author}: ${err.rawMessage.take(100)}")
                val rKey =
                    queueStatusResultNameNormalizeService.buildQueueStatusResultName(ORDER_STATUS_REFUND_PATTERN, err.author)
                sendMessageProducer.sendMessage(rKey, err.rawMessage, props.ordersExchange, null)
            }
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)

            // Reject всех успешных сообщений, чтобы они вернулись в очередь
            successMessages.forEach { channel.basicReject(it.tag, false) }
        }
    }

    private fun <T : Any> parseBatch(
        messages: List<Message>,
        channel: Channel,
        dtoClass: Class<T>,
    ): List<ParsedResult<T>> {
        val result = mutableListOf<ParsedResult<T>>()

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            val messageId = msg.messageProperties.messageId
            val body = String(msg.body, Charsets.UTF_8)

            try {
                val dto = objectMapper.readValue(body, dtoClass)
                result += ParsedResult.Success(tag, dto, messageId)
            } catch (ex: Exception) {
                // Пытаемся вытянуть author вручную
                val author =
                    runCatching {
                        val node = objectMapper.readTree(body)
                        node["metaInfo"]?.firstOrNull()?.get("author")?.asText()
                    }.getOrNull()

                if (author != null) {
                    // Добавляем в Error, НЕ реджектим
                    result += ParsedResult.Error(tag, body, author, messageId)
                } else {
                    // author не нашли → реджектим
                    try {
                        channel.basicReject(tag, false)
                    } catch (ackEx: Exception) {
                        logger.error("Не удалось сделать basicReject для tag=$tag", ackEx)
                    }
                }
            }
        }

        return result
    }

    fun PublishResult.allConfirmed(): Boolean = nAcked.isEmpty() && unconfirmed.isEmpty()
}
