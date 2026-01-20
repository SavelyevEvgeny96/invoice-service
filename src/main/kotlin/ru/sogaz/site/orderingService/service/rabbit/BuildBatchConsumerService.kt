package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedData
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult

interface BuildBatchConsumerService {
    fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderPayloadDto>

    fun searchAndPreparationOrder(parsed: List<ParsedData<RefundPayloadDto>>): RefundPreparationResult
}
