package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.producer.OrderRefundStatusEventProducer

@Component
class OrderRefundStatusSendConsumer(
    private val orderDao: OrderDao,
    private val orderRefundStatusEventProducer: OrderRefundStatusEventProducer,
) {
    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-send-status-refund-order}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    @Transactional(rollbackFor = [Exception::class])
    fun sendOrderStatus(completedPaymentData: CompletedPaymentData) {
        try {
            val order = orderDao.findById(completedPaymentData.orderId) ?: throw OrderNotFoundException(completedPaymentData.orderId)
            orderRefundStatusEventProducer.sendRefundStatus(order, completedPaymentData)
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
