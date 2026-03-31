package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.response.CompletedPaymentInfo
import java.util.UUID

interface OrderInfoService {
    fun getCompletedOrderInfo(orderId: UUID): CompletedPaymentInfo?
}
