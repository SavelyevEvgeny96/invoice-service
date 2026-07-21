package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentMethod

interface PaymentMethodsResolver {
    fun resolve(order: OrderEntity): Set<PaymentMethod>
}
