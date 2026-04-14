package ru.sogaz.site.orderingService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.v2.InvoiceV2Api
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV2
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.mappers.order.CreateOrderResponseMapper
import ru.sogaz.site.orderingService.mappers.order.OrderRequestCommandMapper
import ru.sogaz.site.orderingService.properties.ServiceStatuses
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.order.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

@RestController
@Tag(name = "Order", description = "Управление заказами")
class InvoiceController(
    private val createOrderResponseMapper: CreateOrderResponseMapper,
    private val orderService: OrderService,
    private val authorizationService: AuthorizationService,
    private val orderRequestCommandMapper: OrderRequestCommandMapper,
) : InvoiceV2Api {
    override fun createOrder(
        request: OrderRequestV2,
        authorization: String,
    ): Response<CreateOrderDataV2> {
        request.clientId = authorizationService.checkPermissionByClientId(authorization).externalSystemCode
        return getSuccessResponse(
            getTraceId(),
            ServiceStatuses.STATUS_CODE_SUCCESS,
            createOrderResponseMapper.toV2(
                orderService.createOrderInternal
                    (orderRequestCommandMapper.toCommand(request)),
            ),
        )
    }
}
