package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.receipt.ReceiptService

@Component
class OrderReceiptSendConsumer(
    private val receiptService: ReceiptService,
) {
    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-send-receipt-order}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    fun sendReceipt(completedPaymentData: CompletedPaymentData) {
        try {
            receiptService.sendReceipt(completedPaymentData)
        } catch (ex: OrderNotFoundException) {
            logger.warn(ex.message)
        }
    }

    fun requeue(
        ignore: CompletedPaymentData,
        ex: Exception,
    ): Unit = throw ImmediateRequeueAmqpException(ex)
}
