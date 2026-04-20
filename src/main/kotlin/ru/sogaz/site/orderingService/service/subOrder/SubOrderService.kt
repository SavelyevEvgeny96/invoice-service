package ru.sogaz.site.orderingService.service.subOrder

import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

interface SubOrderService {
    fun createSuborder(
        order: OrderEntity,
        clientId: String,
    ): SubOrderEntity
}
