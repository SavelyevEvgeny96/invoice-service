package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams

interface CardRegistryService {
    fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): String
}
