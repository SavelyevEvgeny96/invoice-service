package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.order.OrderRefundMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.OrderRefundBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer
import java.time.ZoneId

/**
 * Слушатель очереди возвратов.
 */
@Service
class OrderRefundBatchConsumerImpl(
    private val orderDao: OrderDao,
    private val clientSystemDao: ClientSystemDao,
    private val paymentOperationDao: PaymentOperationDao,
    private val subOrderDao: SubOrderDao,
    private val sendMessageProducer: SendMessageProducer,
    private val refundMapper: OrderRefundMapper,
    private val rabbitProps: RabbitProps,
) : OrderRefundBatchConsumer {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val ERR_NO_ACCESS = "У системы отсутствуют права на выполнение операции возврата"
        private const val ERR_NOT_PAID = "Заказ не оплачен"
        private const val ERR_PAYMENT_DAY_EXPIRED = "Платеж недоступен для отмены. Прошло более суток с момента совершения оплаты"
        private const val LOG_INVALID_FORMAT_MISSING_ID = "Получено сообщение неверного формата (invoiceId отсутствует): {}"
        private const val LOG_ORDER_NOT_FOUND = "Заказ не найден для refund. Сообщение: {}"
        private const val LOG_TECHNICAL_ERROR = "Техническая ошибка при обработке refund-сообщения: {}"
    }

    /**
     * Обрабатывает одно сообщение из очереди возвратов.
     *
     * Для доменных ошибок формируется ответ со статусом `error` в `order.queueStatusResultName`.
     * Для технических ошибок исключение пробрасывается выше, чтобы сообщение не терялось
     * и было обработано стандартными retry-механизмами брокера/контейнера.
     */
    @RabbitListener(
        queues = ["\${app.rabbit.queue-invoice-reversal}"],
        containerFactory = "concurrentContainerFactory",
    )
    override fun handleBatchRefundCreated(
        @Payload refundEvent: RefundPayloadDto,
        message: Message,
        channel: Channel,
    ) {
        val raw = message.body.toString(Charsets.UTF_8)
        try {
            val invoiceId = refundEvent.invoiceId ?: run {
                logger.error(LOG_INVALID_FORMAT_MISSING_ID, raw)
                return
            }

            val order = orderDao.findById(invoiceId)
            if (order == null) {
                logger.error(LOG_ORDER_NOT_FOUND, raw)
                return
            }

            val clientSystem = clientSystemDao.findBySystemCode(order.clientId)
            if (clientSystem?.permissionReturn != true) {
                sendError(order, invoiceId, ERR_NO_ACCESS)
                return
            }

            if (!order.status.isPaidFor()) {
                sendError(order, invoiceId, ERR_NOT_PAID)
                return
            }

            val payment = paymentOperationDao.findSuccessPaymentByOrderId(invoiceId)
            if (payment?.payDate == null || !isSameDayNow(payment.payDate!!)) {
                sendError(order, invoiceId, ERR_PAYMENT_DAY_EXPIRED)
                return
            }

            val subOrder = subOrderDao.findFirstByOrderEntityOrderId(invoiceId)
            val description = buildDescription(subOrder)
            val reversalDto = refundMapper.toReversalPaymentDto(payment, description)

            sendMessageProducer.sendMessage(
                rabbitProps.routingKeyReversalPayment,
                reversalDto,
                rabbitProps.paymentsExchange,
                invoiceId,
            )
        } catch (ex: Exception) {
            logger.error(LOG_TECHNICAL_ERROR, raw, ex)
            throw ex
        }
    }

    private fun sendError(
        order: OrderEntity,
        invoiceId: java.util.UUID,
        errorText: String,
    ) {
        val routingKey = order.queueStatusResultName
        val errorDto = refundMapper.toErrorDto(invoiceId, errorText)
        sendMessageProducer.sendMessage(routingKey, errorDto, rabbitProps.ordersExchange, invoiceId)
    }

    private fun isSameDayNow(payDate: java.time.Instant): Boolean {
        val zone = ZoneId.systemDefault()
        return payDate.atZone(zone).toLocalDate() == java.time.LocalDate.now(zone)
    }

    private fun buildDescription(subOrder: SubOrderEntity?): String {
        if (subOrder?.contractNumber.isNullOrBlank()) {
            return "Отмена транзакции по договору"
        }
        val base = "Отмена транзакции по договору №${subOrder?.contractNumber}"
        val contractDate = subOrder?.contractDate?.atZone(ZoneId.systemDefault())?.toLocalDate()
        return if (contractDate != null) "$base от $contractDate" else base
    }
}
