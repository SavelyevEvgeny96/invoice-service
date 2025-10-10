package ru.sogaz.site.orderingService.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.siter.models.resonses.Response

@RestController
class OrdersUserController(
    private val ordersUserService: OrdersUserService,
) {
    @PostMapping("/v1/orders/ordersuser")
    fun getClientOrders(
        @Valid @RequestBody request: OrdersUserRequest,
    ): Response<OrdersUserResponse> = ordersUserService.findOrders(request)
}
