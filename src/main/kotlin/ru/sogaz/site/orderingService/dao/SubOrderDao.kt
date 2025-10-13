package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.SubOrderEntity

interface SubOrderDao {
    fun upsertSubOrders(subs: List<SubOrderEntity>)
    fun findByOrderId(orderId: String): List<SubOrderEntity?>
}
