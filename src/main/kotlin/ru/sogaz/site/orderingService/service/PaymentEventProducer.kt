package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.PublishResult
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent

interface PaymentEventProducer {
    fun sendBatch(events: List<OrderPayloadDto>): PublishResult
}
