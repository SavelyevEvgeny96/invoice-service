package ru.sogaz.site.orderingService.dto.response

import java.math.BigDecimal

/**
 * Корневой объект бизнес-данных ответа /v1/orders/ordersuser
 */
data class OrdersUserResponse(
    val ordersList: List<OrderItem>
)

/**
 * Короткая карточка заказа
 */
data class OrderItem(
    val orderId: String,
    val premiumAmount: BigDecimal
)