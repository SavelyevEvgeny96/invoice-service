package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.ReceiptEntity

interface ReceiptDao {
    fun save(receipt: ReceiptEntity): ReceiptEntity
}
