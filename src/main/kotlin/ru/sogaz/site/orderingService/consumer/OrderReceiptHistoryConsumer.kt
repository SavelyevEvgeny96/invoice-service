package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.SentReceiptData
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.order.OrderStatusService
import ru.sogaz.site.orderingService.service.receipt.ReceiptService

@Component
class OrderReceiptHistoryConsumer(
    private val orderStatusService: OrderStatusService,
    private val receiptService: ReceiptService,
) {
    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-fill-history-receipts}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    fun addHistoryRecord(sentReceiptData: SentReceiptData) {
        try {
            orderStatusService.updateOrderReceiptState(sentReceiptData)
            if (sentReceiptData.receiptId != null) {
                receiptService.saveSentReceiptRecord(sentReceiptData)
            }
        } catch (ex: OrderNotFoundException) {
            logger.warn(ex.message)
        } catch (ex: Exception) {
            logger.error(ex.message)
        }
    }

    fun requeue(
        ignore: SentReceiptData,
        ex: Exception,
    ): Unit = throw ImmediateRequeueAmqpException(ex)
}
