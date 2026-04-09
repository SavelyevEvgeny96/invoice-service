package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.net.URI

interface PayInfoService {
    fun getInfo(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): Pair<URI, PaySbp?>
}
