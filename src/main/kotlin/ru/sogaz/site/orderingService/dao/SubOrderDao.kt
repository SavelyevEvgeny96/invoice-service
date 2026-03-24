package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.util.UUID

interface SubOrderDao {
    fun upsertSubOrders(subs: List<SubOrderEntity>)

    fun findFirstByOrderEntityOrderId(orderId: UUID?): SubOrderEntity?

    fun findByOrderId(orderId: UUID?): List<SubOrderEntity?>
}
