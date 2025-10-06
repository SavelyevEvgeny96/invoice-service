package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.data.PublishResult

interface PaymentEventProducer {
    fun sendBatch(events: List<PaymentCreatedEvent>): PublishResult
}
