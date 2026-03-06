package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderStatusService {
    fun updatePaidOrder(completedPaymentData: CompletedPaymentData): OrderEntity
}
