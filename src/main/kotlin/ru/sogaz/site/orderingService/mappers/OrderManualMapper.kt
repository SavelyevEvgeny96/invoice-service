package ru.sogaz.site.orderingService.mappers

import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.CreateSubOrderCommand
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Component
class OrderManualMapper(
    private val orderMapper: OrderMapper,
) {
    private companion object {
        val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
    }

    fun toOrderEntity(
        command: CreateOrderCommand,
        skipSendingErrors: Boolean,
    ): OrderEntity {
        val order =
            orderMapper.fromCommand(command).apply {
                val subOrders = attachSubOrders(this, command.subOrders)
                this.subOrders.addAll(subOrders)

                skipSendingErrorsQueue = skipSendingErrors
                queueStatusResultName = buildQueueStatusResultName(clientId)
                premiumAmount = calculatePremiumAmount()
            }
        return order
    }

    private fun attachSubOrders(
        order: OrderEntity,
        requests: List<CreateSubOrderCommand>,
    ): List<SubOrderEntity> =
        requests.map { request ->
            orderMapper.fromCommand(request).apply {
                orderEntity = order
            }
        }

    private fun buildQueueStatusResultName(clientId: String?): String? =
        clientId
            ?.takeIf { it.isNotBlank() }
            ?.replace(NON_ALPHANUMERIC_REGEX, ".")
            ?.let { "payment.status.$it.created" }
}
