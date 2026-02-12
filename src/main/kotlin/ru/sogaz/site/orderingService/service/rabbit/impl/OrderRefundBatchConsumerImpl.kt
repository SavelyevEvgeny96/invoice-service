package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.ParsedResultToReceiptMessageDto
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
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val parsedResultToReceiptMessageDto: ParsedResultToReceiptMessageDto,
) : OrderRefundBatchConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val ERROR_MESSAGE_IN_AUTHOR = "Битое сообщение от автора=%s : %s."
        private const val NOT_VALID_BATCH_MESSAGE_REFUND_ORDER =
            "Нет валидных сообщений для обработки " +
                "в батче по возврату заказа "
        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED =
            "Нет валидных сообщений для обработки"

        private const val LOG_PARSE_BATCH_RETURNED_NULL =
            "Парсер батча вернул null. Сообщение невалидно или пустое."

        private const val LOG_REFUND_MESSAGE_RECEIVED =
            "Получено сообщение статуса возврата. orderId=%s, status=%s, tag=%s."

        private const val LOG_ORDER_NOT_FOUND =
            "Заказ не найден. orderId=%s, tag=%s. Сообщение отправлено в DLQ."

        private const val LOG_INVALID_REFUND_STATUS =
            "Невалидный статус возврата. orderId=%s, status=%s, tag=%s. Сообщение отправлено в DLQ."

        private const val LOG_SUB_ORDER_NOT_FOUND =
            "SubOrder не найден или не прошёл проверку. orderId=%s, tag=%s. Сообщение отправлено в DLQ."

        private const val LOG_ORDER_ALREADY_REFUNDED =
            "Заказ уже находится в статусе REFUND (повторная доставка). orderId=%s, tag=%s."

        private const val LOG_ORDER_STATUS_UPDATED_TO_REFUND =
            "Статус заказа обновлён на REFUND. orderId=%s, tag=%s."

        private const val LOG_RECEIPT_MESSAGE_SENT =
            "Сообщение на создание чека отправлено. orderId=%s, exchange=%s, routingKey=%s, tag=%s."

        private const val LOG_MESSAGE_ACKED =
            "Сообщение успешно подтверждено (ACK). orderId=%s, tag=%s."

        private const val LOG_REFUND_PROCESSING_ERROR =
            "Ошибка обработки сообщения статуса возврата. tag=%s, reason=%s. Сообщение будет возвращено в очередь."

        private const val LOG_PARSE_RESULT_ERROR =
            "Ошибка разбора входящего сообщения. tag=%s, details=%s. Сообщение отправлено в DLQ."
    }
    // endregion

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
        // parseBatch теперь возвращает ParsedResult.Success и ParsedResult.Error
        val started = System.nanoTime()
        val parsedResults: List<ParsedResult<RefundPayloadDto>> =
            messages.mapNotNull { msg ->
                sendMessageProducer.parseBatch(
                    msg,
                    channel,
                    RefundPayloadDto::class.java,
                )
            }
        val successMessages = parsedResults.filterIsInstance<ParsedResult.Success<RefundPayloadDto>>()
        val errorMessages = parsedResults.filterIsInstance<ParsedResult.Error>()

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
                        ERROR_MESSAGE_IN_AUTHOR.format(err.author, err.rawMessage),
                    )

                    // Передача битого сообщения во внешнюю систему
                    // (не через DLQ)
                    sendMessageProducer.processErrorMessages(
                        err,
                        channel,
                        props.ordersExchange,
                        ORDER_STATUS_REFUND_PATTERN,
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

    /**
     * RabbitMQ listener для обработки статусов возврата средств по заказам.
     *
     * Алгоритм:
     * 1. Парсинг входящего сообщения и получение deliveryTag.
     * 2. Валидация данных и бизнес-проверки.
     * 3. Обновление статуса заказа на REFUND.
     * 4. Публикация сообщения на создание чека.
     * 5. ACK при успехе, REJECT для фатальных ошибок, NACK с requeue=true для временных.
     */
    @RabbitListener(
        queues = ["\${app.rabbit.queue-payment-status-refund}"],
    )
    override fun handleMessageToOrderRefundStatus(
        messages: Message,
        channel: Channel,
    ) {
        // Парсим входящее сообщение и извлекаем deliveryTag для ручного управления ACK/NACK/REJECT
        val parsedResult = sendMessageProducer.parseBatch(messages, channel, RefundResponseDto::class.java)

        // Если батч невалидный или пустой — логируем и выходим
        if (parsedResult == null) {
            logger.warn(LOG_PARSE_BATCH_RETURNED_NULL)
            return
        }
        when (parsedResult) {
            is ParsedResult.Success -> {
                val tag = parsedResult.tag
                try {
                    val dto = parsedResult.dto

                    logger.info(
                        String.format(
                            LOG_REFUND_MESSAGE_RECEIVED,
                            dto.orderId,
                            dto.status,
                            tag,
                        ),
                    )

                    // 1. Поиск заказа
                    val order =
                        orderDao.findById(dto.orderId)
                            ?: run {
                                logger.warn(
                                    String.format(
                                        LOG_ORDER_NOT_FOUND,
                                        dto.orderId,
                                        tag,
                                    ),
                                )
                                channel.basicReject(tag, false)
                                return
                            }

                    // 2. Проверка статуса сообщения
                    if (dto.status != OrderStatusesEnum.SUCCESS.desc) {
                        logger.warn(
                            String.format(
                                LOG_INVALID_REFUND_STATUS,
                                dto.orderId,
                                dto.status,
                                tag,
                            ),
                        )
                        channel.basicReject(tag, false)
                        return
                    }

                    // 3. Поиск subOrder
                    val subOrder =
                        subOrderDao.findByOrderIdAndMainContractCheck(order.orderId)
                            ?: run {
                                logger.warn(
                                    String.format(
                                        LOG_SUB_ORDER_NOT_FOUND,
                                        order.orderId,
                                        tag,
                                    ),
                                )
                                channel.basicReject(tag, false)
                                return
                            }

                    // 4. Идемпотентность
                    if (order.status == OrderStatusesEnum.REFUND) {
                        logger.info(
                            String.format(
                                LOG_ORDER_ALREADY_REFUNDED,
                                order.orderId,
                                tag,
                            ),
                        )
                        channel.basicAck(tag, false)
                        return
                    }

                    // 5. Обновление статуса заказа
                    order.status = OrderStatusesEnum.REFUND
                    orderDao.save(order)

                    logger.info(
                        String.format(
                            LOG_ORDER_STATUS_UPDATED_TO_REFUND,
                            order.orderId,
                            tag,
                        ),
                    )

                    // 6. Отправка сообщения на создание чека
                    val receiptMessage =
                        parsedResultToReceiptMessageDto.toDto(order, subOrder)

                    sendMessageProducer.sendMessage(
                        props.routingKeyPaymentReceiptCreateCheck,
                        receiptMessage,
                        props.receiptExchange,
                        order.orderId,
                    )
                    logger.info(
                        String.format(
                            LOG_RECEIPT_MESSAGE_SENT,
                            order.orderId,
                            props.receiptExchange,
                            props.routingKeyPaymentReceiptCreateCheck,
                            tag,
                        ),
                    )

                    // 7. Подтверждаем успешную обработку сообщения
                    channel.basicAck(tag, false)
                    logger.info(
                        String.format(
                            LOG_MESSAGE_ACKED,
                            order.orderId,
                            tag,
                        ),
                    )
                } catch (e: Exception) {
                    // ошибка — возвращаем сообщение в очередь
                    logger.error(
                        String.format(
                            LOG_REFUND_PROCESSING_ERROR,
                            tag,
                            e.message,
                        ),
                        e,
                    )
                    channel.basicNack(tag, false, true)
                }
            }

            is ParsedResult.Error -> {
                logger.warn(
                    String.format(
                        LOG_PARSE_RESULT_ERROR,
                        parsedResult.tag,
                        parsedResult.rawMessage,
                    ),
                )
                channel.basicReject(parsedResult.tag, false)
            }
        }
    }
}
