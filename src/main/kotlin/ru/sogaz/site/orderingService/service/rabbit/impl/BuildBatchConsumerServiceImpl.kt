package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundPreparationResult
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
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
    private val clientSystemDao: ClientSystemDao,
) : BuildBatchConsumerService {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val PREFIX_REFUND_ROUTING_KEY = "order.status.refund.%s.created"
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

    override fun searchAndPreparationOrder(parsed: List<RefundPayloadDto>): RefundPreparationResult {
        // 0) Проставляем routingKey всем
        val prepared =
            parsed.map { p ->
                val author = p.metaInfo.firstOrNull()?.author
                val rk = buildRoutingKeyByCustomerId(author, PREFIX_REFUND_ROUTING_KEY)
                p.copy(routingKeyStatus = rk)
            }

        // 1) Собрали orderIds и вытащили ордера
        val orderIds =
            prepared
                .asSequence()
                .map { it.orderId }
                .distinct()
                .toList()
        val ordersById = orderDao.findByIds(orderIds).associateBy { it.orderId }

        // 2) missing = те, у кого ордер НЕ найден
        val (existsInDb, missing) = prepared.partition { ordersById.containsKey(it.orderId) }

        // 3) Для тех, у кого ордер найден — проверяем доступ по author (external_system_code)
        val authors = existsInDb.mapNotNull { it.metaInfo.firstOrNull()?.author }.distinct()

        val allowedAuthors: Set<String> =
            if (authors.isEmpty()) {
                emptySet()
            } else {
                clientSystemDao
                    .checkingRefundAccess(authors)
                    .map { it.externalSystemCode }
                    .toSet()
            }

        // 4) noAccess / found (ТОЛЬКО среди тех, у кого ордер найден и есть доступ)
        val (foundWithAccess, noAccess) =
            existsInDb.partition { p ->
                val author = p.metaInfo.firstOrNull()?.author
                author != null && author in allowedAuthors
            }

        // 5) notForPaid (НЕ оплаченные) / found (оплаченные)
        val (foundRaw, notForPaid) =
            foundWithAccess.partition { p ->
                val order = ordersById[p.orderId]
                order != null && order.status?.isPaidFor() == true
            }

        val found =
            foundRaw.map { p ->
                val order = ordersById[p.orderId]!!
                // Если нужно дополнительно что-то скопировать из ордера, делаем здесь
                p.copy(orderId = order.orderId)
            }

        return RefundPreparationResult(
            found = found,
            missing = missing,
            noAccess = noAccess,
            notForPaid = notForPaid,
        )
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
