package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.PublishResult

interface PaymentEventProducer {
    fun sendBatchOrderCreated(events: List<OrderPayloadDto>): PublishResult
}
