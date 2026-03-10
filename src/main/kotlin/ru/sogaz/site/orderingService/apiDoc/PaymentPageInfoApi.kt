package ru.sogaz.site.orderingService.apiDoc

import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface PaymentPageInfoApi {
    fun getInfoPage(
        @PathVariable orderId: UUID,
        payQueryParams: PayQueryParams,
    ): Response<DataOrderPaymentPageInfo>
}