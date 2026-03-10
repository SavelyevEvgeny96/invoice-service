package ru.sogaz.site.orderingService.controller

import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.apiDoc.PaymentPageInfoApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

@RestController
class PaymentPageInfoController: PaymentPageInfoApi {
    override fun getInfoPage(
        orderId: UUID,
        payQueryParams: PayQueryParams
    ): Response<DataOrderPaymentPageInfo> {
        TODO("Not yet implemented")
    }
}