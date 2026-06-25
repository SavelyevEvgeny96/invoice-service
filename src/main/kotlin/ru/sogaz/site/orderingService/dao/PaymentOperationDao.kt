package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import java.util.UUID

interface PaymentOperationDao {
    fun save(paymentOperation: PaymentOperationEntity): PaymentOperationEntity

    fun findSuccessPaymentByOrderId(orderId: UUID): PaymentOperationEntity?

    fun findLastByOrderId(orderId: UUID): PaymentOperationEntity?
}
