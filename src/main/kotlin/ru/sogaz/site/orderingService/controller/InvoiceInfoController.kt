package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.apiDoc.InvoiceInfoApi
import ru.sogaz.site.orderingService.dto.response.CompletedPaymentInfo
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.order.OrderInfoService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.util.UUID

@RestController
class InvoiceInfoController(
    private val orderInfoService: OrderInfoService,
    private val authorizationService: AuthorizationService,
) : InvoiceInfoApi {
    companion object {
        private const val SUCCESS_CODE = 1101521200
    }

    override fun getCompletedOrderInfo(
        authorization: String,
        orderId: UUID,
    ): Response<CompletedPaymentInfo?> {
        authorizationService.checkPermissionByClientId(authorization)
        return orderInfoService
            .getCompletedOrderInfo(orderId)
            .wrapToSuccessResponse(SUCCESS_CODE)
    }

    private fun <T> T.wrapToSuccessResponse(statusCode: Int): Response<T> = getSuccessResponse(getTraceId(), statusCode, this)
}
