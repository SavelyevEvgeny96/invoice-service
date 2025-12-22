package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.ParsedData
import ru.sogaz.site.orderingService.dto.data.Split
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.request.RefundPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService

@Service
class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val props: RabbitProps,
    private val orderMapper: OrderMapper,
    private val paymentEventMapper: PaymentEventMapper,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val PREFIX_REFUND_ROUTING_KEY = "order.status.refund.%d.created"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun insertBatchOrderCreated(batch: List<OrderPayloadDto>): List<OrderPayloadDto> {
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

    override fun searchAndPreparationOrder(parsed: List<ParsedData<RefundPayloadDto>>): Split<RefundPayloadDto> {
        // 0) Сначала проставляем routingKey для каждого сообщения (и для found, и для missing)
        val prepared: List<ParsedData<RefundPayloadDto>> =
            parsed.map { p ->
                val author = p.dto.metaInfo.author
                val rk = buildRoutingKeyByCustomerId(author, PREFIX_REFUND_ROUTING_KEY)

                p.copy(dto = p.dto.copy(routingKey = rk))
            }

        // 1) Собрали UUID
        val orderIds =
            prepared
                .asSequence()
                .map { it.dto.orderId }
                .distinct()
                .toList()

        // 2) Достали ордера и сделали map для быстрых lookup
        val ordersById = orderDao.findByIds(orderIds).associateBy { it.orderId }

        // 3) Разделили сообщения: найден / не найден
        val (found, missing) = prepared.partition { ordersById.containsKey(it.dto.orderId) }

        return Split(found, missing)
    }

    private fun buildRoutingKeyByCustomerId(
        clientId: String?,
        prefix: String,
    ): String? {
        if (clientId.isNullOrBlank()) return null

        val normalizedClientId = clientId.replace(Regex("[^A-Za-zА-Яа-яЁё0-9]"), ".")

        return prefix.format(normalizedClientId)
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

            val subOrders = dto.subOrders.map { orderMapper.toSubOrderEntity(it, order) }
            subs += subOrders
        }
        return orders to subs
    }
}
