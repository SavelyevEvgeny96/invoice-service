package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
open class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val props: RabbitProps,
    private val orderMapper: OrderMapper,
    private val paymentEventMapper: PaymentEventMapper,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>): List<OrderPayloadDto> {
        if (batch.isEmpty()) return emptyList()

        val (orders, subs) = prepareEntities(batch)
        logger.info(LOG_START.format(batch.size))

        if (orders.isNotEmpty()) {
            val orderIds = orderDao.upsertOrdersReturningIds(orders)
            orders.forEachIndexed { index, order ->
                order.orderId = orderIds[index]
            }
        }

        if (subs.isNotEmpty()) {
            subOrderDao.upsertSubOrders(subs)
        }

        // Возвращаем те же DTO, но с заполненным orderIdRecurrent
        return enrichDtosWithOrderIds(batch, orders)
    }

    private fun enrichDtosWithOrderIds(
        batch: List<OrderPayloadDto>,
        orders: List<OrderEntity>,
    ): List<OrderPayloadDto> =
        batch.mapIndexed { index, dto ->
            val orderId = orders.getOrNull(index)?.orderId
            dto.copy(orderIdRecurrent = orderId)
        }
    // Оставляем метод когда все переедет в сервис то будем в очередь отправлять этот DTO
    private fun mapToPaymentEvents(
        orders: List<OrderEntity>,
        nowIso: String,
    ): List<PaymentCreatedEvent> = orders.map { paymentEventMapper.toPaymentEvent(it, nowIso, props.routingKeyPayment) }

    private fun prepareEntities(batch: List<OrderPayloadDto>): Pair<List<OrderEntity>, List<SubOrderEntity>> {
        val orders = mutableListOf<OrderEntity>()
        val subs = mutableListOf<SubOrderEntity>()

        batch.forEach { dto ->
            val order = orderMapper.toOrderEntity(dto)
            orders += order

            val subOrders = dto.subOrders.map { orderMapper.toSubOrderEntity(it, order, dto.managerEmail) }
            subs += subOrders
        }
        return orders to subs
    }
}
