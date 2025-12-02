package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.siter.models.resonses.Response

interface OrdersUserService {
    fun findOrders(request: OrdersUserRequest): Response<OrdersUserResponse>
}
