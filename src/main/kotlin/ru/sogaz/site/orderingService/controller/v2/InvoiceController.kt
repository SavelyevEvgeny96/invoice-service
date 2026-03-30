package ru.sogaz.site.orderingService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.apiDoc.v2.InvoiceV2Api
import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.mappers.order.OrderRequestCommandMapper
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.OrderService
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Order", description = "Управление заказами")
class InvoiceController(
    private val orderService: OrderService,
    private val authorizationService: AuthorizationService,
    private val orderRequestCommandMapper: OrderRequestCommandMapper,
) : InvoiceV2Api {
    override fun createOrder(
        request: OrderRequestV2,
        authorization: String,
    ): Response<DataOrder> {
        request.clientId = authorizationService.checkPermissionByClientId(authorization).externalSystemCode
        return orderService.createOrder(orderRequestCommandMapper.toCommand(request))
    }
}
