package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.payment.client.model.DataPay
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams

interface CardRegistryService {
    fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): DataPay
}
