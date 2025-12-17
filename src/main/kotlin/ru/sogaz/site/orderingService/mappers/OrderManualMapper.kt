package ru.sogaz.site.orderingService.mappers

import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.site.orderingService.dto.request.SubOrderRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Component
class OrderManualMapper(
    private val orderMapper: OrderMapper,
) {
    private companion object {
        val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
    }

    fun toOrderEntity(orderRequest: OrderRequest): OrderEntity =
        orderMapper.fromRequestDto(orderRequest).apply {
            val subOrders = attachSubOrders(this, orderRequest.orders)
            this.subOrders.addAll(subOrders)

            queueStatusResultName = buildQueueStatusResultName(clientId)
            premiumAmount = calculatePremiumAmount()
        }

    private fun attachSubOrders(
        order: OrderEntity,
        requests: List<SubOrderRequest>,
    ): List<SubOrderEntity> =
        requests.map { request ->
            orderMapper.fromRequestDto(request).apply {
                orderEntity = order
            }
        }

    private fun buildQueueStatusResultName(clientId: String?): String? =
        clientId
            ?.takeIf { it.isNotBlank() }
            ?.replace(NON_ALPHANUMERIC_REGEX, "")
            ?.let { "payment.status.$it.created" }
}
