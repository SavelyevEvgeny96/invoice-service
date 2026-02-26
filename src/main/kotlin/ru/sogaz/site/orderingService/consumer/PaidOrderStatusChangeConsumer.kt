package ru.sogaz.site.orderingService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.ImmediateRequeueAmqpException
import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.order.OrderStatusService

@Component
class PaidOrderStatusChangeConsumer(
    private val orderStatusService: OrderStatusService,
) {
    private val logger = loggerFor(javaClass)

//    @RabbitListener(
//        queues = ["\${app.rabbit.queue-change-status-order}"],
//        containerFactory = "concurrentContainerFactory",
//    )
    @Retry(name = "rabbitConsumerRetry", fallbackMethod = "requeue")
    fun sendReceipt(completedPaymentData: CompletedPaymentData) {
        try {
            orderStatusService.updatePaidOrder(completedPaymentData)
        } catch (ex: OrderNotFoundException) {
            logger.warn(ex.message)
        }
    }

    fun requeue(
        ignore: CompletedPaymentData,
        ex: Exception,
    ): Unit = throw ImmediateRequeueAmqpException(ex)
}
