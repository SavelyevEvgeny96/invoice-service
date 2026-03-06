package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.entity.OrderEntity

interface BuildBatchConsumerService {
    fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderEntity>

    fun searchAndPreparationOrder(parsed: List<RefundPayloadDto>): RefundPreparationResult
}
