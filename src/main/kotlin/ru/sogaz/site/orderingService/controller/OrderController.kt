package ru.sogaz.site.orderingService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.OrderV1Api
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV1
import ru.sogaz.site.orderingService.dto.request.OrderRequestV1
import ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus
import ru.sogaz.site.orderingService.mappers.order.CreateOrderResponseMapper
import ru.sogaz.site.orderingService.mappers.order.OrderRequestCommandMapper
import ru.sogaz.site.orderingService.properties.ServiceStatuses
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.util.UUID

@RestController
@Tag(name = "Order", description = "Управление заказами")
class OrderController(
    private val createOrderResponseMapper: CreateOrderResponseMapper,
    private val orderService: OrderService,
    private val authorizationService: AuthorizationService,
    private val orderRequestCommandMapper: OrderRequestCommandMapper,
) : OrderV1Api {
    override fun createOrder(
        request: OrderRequestV1,
        authorization: String,
    ): Response<CreateOrderDataV1> {
        request.clientId = authorizationService.checkPermissionByClientId(authorization).externalSystemCode
        return getSuccessResponse(
            getTraceId(),
            ServiceStatuses.STATUS_CODE_SUCCESS,
            createOrderResponseMapper.toV1(
                orderService.createOrderInternal(
                    orderRequestCommandMapper.toCommand(request),
                ),
            ),
        )
    }

    override fun getOrderStatus(orderId: UUID): Response<DataGetOrderStatus> {
        val getOrderStatus = orderService.getOrderStatus(orderId)
        return getSuccessResponse(
            getTraceId(),
            ServiceStatuses.STATUS_CODE_SUCCESS,
            getOrderStatus,
        )
    }
}
