package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.request.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent

interface BuildBatchConsumerService {
    fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent>
}
