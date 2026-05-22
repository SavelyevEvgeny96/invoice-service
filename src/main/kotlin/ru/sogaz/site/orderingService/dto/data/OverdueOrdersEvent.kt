package ru.sogaz.site.orderingService.dto.data

import ru.sogaz.site.orderingService.entity.OrderEntity

data class OverdueOrdersEvent(
    val orders: List<OrderEntity>,
)
