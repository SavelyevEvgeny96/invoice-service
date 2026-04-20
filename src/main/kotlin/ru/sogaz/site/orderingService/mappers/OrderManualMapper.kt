package ru.sogaz.site.orderingService.mappers

import org.springframework.stereotype.Component
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.CreateSubOrderCommand
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.PAYMENT_STATUS_PATTERN
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class OrderManualMapper(
    private val orderMapper: OrderMapper,
    private val queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService,
) {
    fun toOrderEntity(
        command: CreateOrderCommand,
        skipSendingErrors: Boolean,
    ): OrderEntity =
        orderMapper.fromCommand(command).apply {
            skipSendingErrorsQueue = skipSendingErrors
            queueStatusResultName =
                queueStatusResultNameNormalizeService.buildQueueStatusResultName(PAYMENT_STATUS_PATTERN, clientId)
            premiumAmount = calculatePremiumAmount(command.subOrders)
        }

    fun toSubOrderEntities(
        order: OrderEntity,
        subOrders: List<CreateSubOrderCommand>,
    ): List<SubOrderEntity> =
        subOrders.map { subOrderCommand ->
            orderMapper.fromCommand(subOrderCommand).apply {
                orderEntity = order
            }
        }

    private fun calculatePremiumAmount(subOrders: List<CreateSubOrderCommand>): BigDecimal =
        subOrders
            .sumOf { it.premiumAmount }
            .setScale(2, RoundingMode.HALF_UP)
}
