package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse

interface OrdersUserService {
    fun findOrders(request: OrdersUserRequest): OrdersUserResponse
}
