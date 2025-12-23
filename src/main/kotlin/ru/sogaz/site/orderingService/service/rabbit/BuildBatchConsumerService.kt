package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedData
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto

interface BuildBatchConsumerService {
    fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderPayloadDto>

    fun searchAndPreparationOrder(parsed: List<ParsedData<RefundPayloadDto>>): RefundPreparationResult
}
