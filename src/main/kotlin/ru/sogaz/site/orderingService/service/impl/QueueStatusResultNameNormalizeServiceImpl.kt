package ru.sogaz.site.orderingService.service.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService

@Service
class QueueStatusResultNameNormalizeServiceImpl(
    @Qualifier("RKeyRegexp")
    private val regex: Regex,
) : QueueStatusResultNameNormalizeService {
    companion object {
        const val PAYMENT_STATUS_PATTERN = "payment.status.%s.created"
        const val ORDER_STATUS_REFUND_PATTERN = "order.status.refund.%s.created"
    }

    override fun buildQueueStatusResultName(
        pattern: String,
        clientId: String,
    ): String {
        val normalizedString = clientId.replace(regex, ".")
        return String.format(pattern, normalizedString)
    }
}
