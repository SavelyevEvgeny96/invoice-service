package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.ReceiptEntity
import java.util.UUID

interface ReceiptDao {
    fun save(receipt: ReceiptEntity): ReceiptEntity

    fun findReceiptsByOrderId(orderId: UUID): List<ReceiptEntity>
}
