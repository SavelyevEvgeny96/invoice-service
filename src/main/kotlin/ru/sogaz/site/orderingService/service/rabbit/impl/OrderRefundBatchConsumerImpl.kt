package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_REFUND_PATTERN
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.rabbit.OrderRefundBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Service
class OrderRefundBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProps,
) : OrderRefundBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val ERROR_MESSAGE_IN_AUTHOR = "Битое сообщение от автора=%s : %s."
        private const val NOT_VALID_BATCH_MESSAGE_REFUND_ORDER =
            "Нет валидных сообщений для обработки " +
                    "в батче по возврату заказа "
    }

    private val logger = loggerFor(OrderRefundBatchConsumerImpl::class.java)

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
        val parsedResults = sendMessageProducer.parseBatch(messages, channel, RefundPayloadDto::class.java)

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

            // ---------- Обработка битых сообщений с author ----------
            if (errorMessages.isNotEmpty()) {
                errorMessages.forEach { err ->
                    logger.warn(
                        ERROR_MESSAGE_IN_AUTHOR.format(err.author, err.rawMessage)
                    )

                    // Передача битого сообщения во внешнюю систему
                    // (не через DLQ)
                    sendMessageProducer.processErrorMessages(
                        err,
                        channel,
                        props.paymentsExchange,
                        ORDER_STATUS_REFUND_PATTERN
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error(
                "Ошибка при обработке валидных сообщений батча: ${ex.message}",
                ex
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