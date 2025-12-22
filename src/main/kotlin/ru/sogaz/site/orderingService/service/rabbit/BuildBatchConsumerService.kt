package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.Parsed
import ru.sogaz.site.orderingService.dto.data.Split
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto

interface BuildBatchConsumerService {
    fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderPayloadDto>

    fun searchAndPreparationOrder(parsed: List<Parsed<RefundPayloadDto>>): Split<RefundPayloadDto>
}
