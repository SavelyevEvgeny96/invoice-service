package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService

@Service
class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val orderMapper: OrderMapper,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderEntity> {
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

        return orders
    }

    private fun prepareEntities(batch: List<OrderPayloadDto>): Pair<List<OrderEntity>, List<SubOrderEntity>> {
        val orders = mutableListOf<OrderEntity>()
        val subs = mutableListOf<SubOrderEntity>()

        batch.forEach { dto ->
            val order = orderMapper.toOrderEntity(dto)
            orders += order

            val subOrders = dto.subOrders.map { orderMapper.toSubOrderEntity(it, order) }
            subs += subOrders
        }
        return orders to subs
    }
}
