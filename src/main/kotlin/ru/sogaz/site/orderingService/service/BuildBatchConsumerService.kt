package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface BuildBatchConsumerService {
    fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderPayloadDto>
}
