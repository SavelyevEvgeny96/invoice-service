package ru.sogaz.site.orderingService.producer

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderPaymentStatusEventProducer {
    fun sendPaymentOrderEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    )
}
