package ru.sogaz.site.orderingService.dto.response

import java.math.BigDecimal
import java.util.UUID

/**
 * Корневой объект бизнес-данных ответа /v1/orders/ordersuser
 */
data class OrdersUserResponse(
    val ordersList: List<OrderItem>,
)

/**
 * Короткая карточка заказа
 */
data class OrderItem(
    val orderId: UUID?,
    val premiumAmount: BigDecimal?,
    val subOrdersList: List<SubOrderItem> = emptyList()
)
/**
 * Под карточки заказа
 */
data class SubOrderItem(
    val policyId: String,
    val policyNumber: String,
    val typeInsurance: String?,
    val premiumAmount: BigDecimal?
)
