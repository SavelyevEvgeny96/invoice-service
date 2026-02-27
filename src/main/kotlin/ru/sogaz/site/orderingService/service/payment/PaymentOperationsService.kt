package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

interface PaymentOperationsService {
    fun saveOperation(completedPaymentData: CompletedPaymentData): OrderEntity
}
