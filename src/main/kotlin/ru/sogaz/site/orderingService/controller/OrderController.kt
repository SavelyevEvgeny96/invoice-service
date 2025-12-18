package ru.sogaz.site.orderingService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.apiDoc.OrderV1Api
import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.OrderService
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Order", description = "Управление заказами")
class OrderController(
    private val orderService: OrderService,
    private val authorizationService: AuthorizationService,
) : OrderV1Api {
    override fun createOrder(
        request: OrderRequest,
        authorization: String,
    ): Response<DataOrder> {
        request.clientId = authorizationService.checkPermissionByClientId(authorization).externalSystemCode
        return orderService.createOrder(request)
    }
}
