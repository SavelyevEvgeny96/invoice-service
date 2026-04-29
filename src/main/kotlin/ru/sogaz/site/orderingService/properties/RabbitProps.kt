package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    lateinit var ordersExchange: String
    lateinit var receiptExchange: String
    lateinit var paymentsExchange: String
    lateinit var queueOrder: String
    lateinit var queueOrderDlq: String
    lateinit var queueInvoiceReversal: String
    lateinit var queuePayment: String
    lateinit var queuePaymentStatusRefund: String
    lateinit var routingKeyOrder: String
    lateinit var routingKeyOrderDlq: String
    lateinit var routingKeyInvoiceReversal: String
    lateinit var routingKeyPayment: String
    lateinit var routingKeyReceipt: String
    lateinit var routingKeyRefundPayment: String
    lateinit var routingKeyPaymentStatusRefund: String
    lateinit var routingKeyPaymentReceiptCreateCheck: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentsExchange = $paymentsExchange")
        logger.info("ordersExchange = $ordersExchange")
        logger.info("queueOrder = $queueOrder")
        logger.info("queueInvoiceReversal = $queueInvoiceReversal")
        logger.info("queuePaymentStatusRefund = $queuePaymentStatusRefund")
        logger.info("queueOrderDlq = $queueOrderDlq")
        logger.info("queuePayment = $queuePayment")
        logger.info("routingKeyOrder = $routingKeyOrder")
        logger.info("routingKeyOrderDlq = $routingKeyOrderDlq")
        logger.info("routingKeyPayment = $routingKeyPayment")
        logger.info("routingKeyInvoiceReversal = $routingKeyInvoiceReversal")
        logger.info("routingKeyRefundPayment = $routingKeyRefundPayment")
        logger.info("routingKeyPaymentStatusRefund = $routingKeyPaymentStatusRefund")
    }
}
