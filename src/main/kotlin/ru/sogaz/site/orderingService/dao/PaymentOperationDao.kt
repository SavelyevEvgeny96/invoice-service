package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.PaymentOperationEntity

interface PaymentOperationDao {
    fun save(paymentOperation: PaymentOperationEntity): PaymentOperationEntity
}
