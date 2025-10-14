package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderDao {
    fun findByRecipientUserId(userId: String): List<OrderEntity?>

    fun findByRecipientGdId(gdId: String): List<OrderEntity?>

    fun findByEmailOrPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?>

    fun findByEmailAndPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?>

    fun upsertOrders(orders: List<OrderEntity>)
}
