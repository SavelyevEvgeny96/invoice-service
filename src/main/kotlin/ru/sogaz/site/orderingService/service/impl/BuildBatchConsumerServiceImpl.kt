package ru.sogaz.site.orderingService.service.impl

import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import java.time.OffsetDateTime
import java.time.ZoneOffset

open class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val props: RabbitProps,
    private val orderMapper: OrderMapper,
    private val paymentEventMapper: PaymentEventMapper,
) : BuildBatchConsumerService {
    override fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent> {
        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
        val orders = saveBatchTransactional(batch)
        return mapToPaymentEvents(orders, nowIso)
    }

    @Transactional(rollbackFor = [Exception::class])
    protected fun saveBatchTransactional(batch: List<OrderPayloadDto>): List<OrderEntity> {
        val (orders, subs) = prepareEntities(batch)
        if (orders.isNotEmpty()) orderDao.upsertOrders(orders)
        if (subs.isNotEmpty()) subOrderDao.upsertSubOrders(subs)
        return orders
    }

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

            val subOrders = dto.subOrders.map { orderMapper.toSubOrderEntity(it, order) }
            subs += subOrders
        }
        return orders to subs
    }
}
