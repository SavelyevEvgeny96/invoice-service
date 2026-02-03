package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    lateinit var ordersExchange: String
    lateinit var paymentsExchange: String
    lateinit var queueOrder: String
    lateinit var queueOrderRefund: String
    lateinit var queuePayment: String
    lateinit var routingKeyOrder: String
    lateinit var routingKeyOrderRefund: String
    lateinit var routingKeyPayment: String
    lateinit var routingKeyRefundPayment: String
    lateinit var routingKeyPaymentStatusRefund: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentsExchange = $paymentsExchange")
        logger.info("ordersExchange = $ordersExchange")
        logger.info("queueOrder = $queueOrder")
        logger.info("queueOrderRefund = $queueOrderRefund")
        logger.info("queuePayment = $queuePayment")
        logger.info("routingKeyOrder = $routingKeyOrder")
        logger.info("routingKeyPayment = $routingKeyPayment")
        logger.info("routingKeyOrderRefund = $routingKeyOrderRefund")
        logger.info("routingKeyRefundPayment = $routingKeyRefundPayment")
        logger.info("routingKeyPaymentStatusRefund = $routingKeyPaymentStatusRefund")
    }
}
