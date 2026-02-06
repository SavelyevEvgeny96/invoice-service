package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.OrderEntity
import java.util.Optional
import java.util.UUID

interface OrderDao {
    fun findByRecipientUserId(userId: String): List<OrderEntity?>

    fun findById(id: UUID): Optional<OrderEntity>

    fun findByUnifiedId(unifiedId: String): List<OrderEntity?>

    fun findByIds(ids: List<UUID?>): List<OrderEntity>

    fun findByEmailOrPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?>

    fun findByEmailAndPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?>

    fun save(order: OrderEntity): OrderEntity

    fun upsertOrdersReturningIds(orders: List<OrderEntity>): List<UUID>
}
