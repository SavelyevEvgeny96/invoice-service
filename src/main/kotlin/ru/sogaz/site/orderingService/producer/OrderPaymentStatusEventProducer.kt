package ru.sogaz.site.orderingService.producer

import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderPaymentStatusEventProducer {
    fun sendPaymentOrderEvent(
        order: OrderEntity,
        errorText: String? = null,
    )
}
