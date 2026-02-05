package ru.sogaz.site.orderingService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.PAYMENT_STATUS_PATTERN
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.rabbit.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Service
class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val objectMapper: ObjectMapper,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProps,
) : OrderBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val ERROR_MESSAGE_IN_AUTHOR = "Битое сообщение от автора=%s : %s."
        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED =
            "Нет валидных сообщений для обработки" +
                    " в батче по созданию заказа"
        private const val NOT_VALID_BATCH_MESSAGE_REFUND_ORDER =
            "Нет валидных сообщений для обработки " +
                    "в батче по возврату заказа "
        val AUTHOR_REGEX =
            Regex(
                """"author"\s*:\s*"([^"]+)"""",
                RegexOption.IGNORE_CASE,
            )
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
        // Замер общего времени обработки batch
        val started = System.nanoTime()
        // Логируем deliveryTag каждого сообщения — важно для отладки ACK/Reject
        logger.info(
            "BATCH RECEIVED: size=${messages.size}, tags=${messages.map { it.messageProperties.deliveryTag }}",
        )
        // Парсинг batch:
        //  - Success -> валидные DTO + deliveryTag
        //  - Error   -> битые сообщения, где удалось извлечь author
        // Сообщения без author могут быть сразу rejected внутри parseBatch
        val parsedResults: List<ParsedResult<OrderPayloadDto>> =
            messages.mapNotNull { msg ->
                sendMessageProducer.parseBatch(
                    msg,
                    channel,
                    OrderPayloadDto::class.java
                )
            }
        val successMessages =
            parsedResults.filterIsInstance<ParsedResult.Success<OrderPayloadDto>>()

        val errorMessages =
            parsedResults.filterIsInstance<ParsedResult.Error<OrderPayloadDto>>()

        // Если batch не содержит ни валидных, ни обработанных битых сообщений — выходим
        if (successMessages.isNotEmpty() && errorMessages.isNotEmpty()) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            return
        }
        try {
            // ---------- Обработка валидных сообщений ----------
            if (successMessages.isNotEmpty()) {
                // Извлекаем DTO для пакетной вставки в БД
                val successDtos = successMessages.map { it.dto }

                // Сохраняем данные и получаем события для отправки
                val events =
                    buildBatchConsumerService.insertBatchOrderCreated(successDtos)

                if (events.isNotEmpty()) {
                    // Отправка всех событий в payments exchange
                    events.forEach { event ->
                        sendMessageProducer.sendMessage(
                            props.routingKeyPayment,
                            event,
                            props.paymentsExchange,
                            event.orderIdRecurrent,
                        )
                    }
                    // ACK выполняется по deliveryTag последнего успешного сообщения
                    // multiple=true -> подтверждаются все сообщения с меньшим deliveryTag
                    val lastTag = successMessages.last().tag
                    channel.basicAck(lastTag, true)
                } else {
                    // Ситуация, когда БД отработала, но события не сформированы
                    // ACK в этом случае не выполняется
                    logger.warn("Не все сообщения подтверждены, batch не ACK")
                }
            }
            // ---------- Обработка битых сообщений с author ----------
            if (errorMessages.isNotEmpty()) {
                errorMessages.forEach { err ->
                    logger.warn(
                        ERROR_MESSAGE_IN_AUTHOR.format(err.author, err.rawMessage),
                    )

                    // Передача битого сообщения во внешнюю систему
                    // (не через DLQ)
                    sendMessageProducer.processErrorMessages(
                        err,
                        channel,
                        props.paymentsExchange,
                        PAYMENT_STATUS_PATTERN,
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error(
                "Ошибка при обработке валидных сообщений батча: ${ex.message}",
                ex,
            )
            // При ошибке возвращаем ВСЕ валидные сообщения в очередь
            // basicReject с multiple=true откатит их для повторной обработки
            val lastTag = successMessages.lastOrNull()?.tag
            lastTag?.let {
                channel.basicReject(it, true)
            }
        } finally {
            // Итоговый лог по batch
            val tookMs = (System.nanoTime() - started) / 1_000_000
            val totalMessages = successMessages.size + errorMessages.size
            logger.info(BATCH_SUMMARY.format(totalMessages, tookMs))
        }
    }


}
