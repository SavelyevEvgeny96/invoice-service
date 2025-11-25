package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface BuildBatchConsumerService {
    fun upsertBatch(batch: List<OrderPayloadDto>): List<OrderPayloadDto>
}
