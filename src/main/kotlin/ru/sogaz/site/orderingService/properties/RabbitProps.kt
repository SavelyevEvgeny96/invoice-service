package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    var ordersExchange: String = "order.exchange"
    var paymentsExchange: String = "payment.exchange"
    var queueOrder: String = "order.created.queue"
    var queuePayment: String = "payment.created.queue"
    var routingKeyOrder: String = "order.created"
    var routingKeyPayment: String = "payment.status.created"

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentsExchange = $paymentsExchange")
        logger.info("ordersExchange = $ordersExchange")
        logger.info("queueOrder = $queueOrder")
        logger.info("queuePayment = $queuePayment")
        logger.info("routingKeyOrder = $routingKeyOrder")
        logger.info("routingKeyPayment = $routingKeyPayment")
    }
}
