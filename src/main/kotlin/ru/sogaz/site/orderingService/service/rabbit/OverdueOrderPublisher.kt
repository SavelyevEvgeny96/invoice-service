package ru.sogaz.site.orderingService.service.rabbit

import ru.sogaz.site.orderingService.entity.OrderEntity

interface OverdueOrderPublisher {
    fun publish(orders: List<OrderEntity>)
}
