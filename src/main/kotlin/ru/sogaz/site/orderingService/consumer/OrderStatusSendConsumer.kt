package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.enums.ApiVersionEnum
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.producer.InvoicePaymentStatusRegEventProducer
import ru.sogaz.site.orderingService.producer.InvoicePaymentStatusReversalEventProducer
import ru.sogaz.site.orderingService.producer.OrderPaymentStatusEventProducer

@Component
@Transactional
class OrderStatusSendConsumer(
    private val orderDao: OrderDao,
    private val orderStatusEventProducer: OrderPaymentStatusEventProducer,
    private val invoiceStatusEventProducer: OrderPaymentStatusEventProducer,
    private val invoiceStatusEventRegProducer: InvoicePaymentStatusRegEventProducer,
    private val invoicePaymentStatusReversalEventProducer: InvoicePaymentStatusReversalEventProducer,
) {
    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-send-status-order}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    fun sendOrderStatus(completedPaymentData: CompletedPaymentData) {
        try {
            val order =
                orderDao.findById(completedPaymentData.orderId) ?: throw OrderNotFoundException(
                    completedPaymentData.orderId,
                )
            if (order.regCard) {
                invoiceStatusEventRegProducer.sendPaymentStatusRegEvent(order, completedPaymentData)
            } else if (completedPaymentData.operationType == OperationTypeEnum.REVERSAL) {
                invoicePaymentStatusReversalEventProducer.sendPaymentStatusReversalEvent(order, completedPaymentData)
            } else {
                when (order.versionApi) {
                    ApiVersionEnum.V2 -> invoiceStatusEventProducer.sendPaymentOrderEvent(order, completedPaymentData)
                    else -> orderStatusEventProducer.sendPaymentOrderEvent(order, completedPaymentData)
                }
            }
        } catch (ex: OrderNotFoundException) {
            logger.warn(ex.message)
        } catch (ex: IllegalArgumentException) {
            logger.error(ex.message)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            throw ex
        }
    }

    fun requeue(
        ignore: CompletedPaymentData,
        ex: Exception,
    ): Unit = throw ImmediateRequeueAmqpException(ex)
}
