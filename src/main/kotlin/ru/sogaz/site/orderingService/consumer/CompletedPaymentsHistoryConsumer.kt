package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.payment.PaymentOperationsService

@Component
class CompletedPaymentsHistoryConsumer(
    private val paymentOperationsService: PaymentOperationsService,
) {
    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-fill-history-payment-order}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    fun addHistoryRecord(completedPaymentData: CompletedPaymentData) {
        try {
            paymentOperationsService.saveOperation(completedPaymentData)
        } catch (ex: OrderNotFoundException) {
            logger.warn(ex.message)
        }
    }

    fun requeue(
        ignore: CompletedPaymentData,
        ex: Exception,
    ): Unit = throw ImmediateRequeueAmqpException(ex)
}
