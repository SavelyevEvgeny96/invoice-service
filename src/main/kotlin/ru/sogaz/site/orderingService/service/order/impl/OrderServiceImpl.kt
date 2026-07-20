package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_ALREADY_PAID
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CLOSED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_NOT_FOUND
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.CreateOrderResult
import ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.ApiVersionEnum
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.properties.PaymentApiProperties
import ru.sogaz.site.orderingService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_PATTERN
import ru.sogaz.site.orderingService.service.order.OrderService
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.shortLinks.ShortLinksIntegration
import ru.sogaz.site.shortlinks.client.model.ShortLinkRequest
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Метод для создания заказа.
 * @param CreateOrderCommand Данные о заказе(содержит внутри лист CreateSubOrderCommand)
 * @throws Exception Если данные невалидны или произошла ошибка при сохранении
 * @return Объект DataOrder, содержащий информацию о платежном запросе
 */
@Service
@Transactional(rollbackFor = [Exception::class])
class OrderServiceImpl(
    private val orderDao: OrderDao,
    private val orderManualMapper: OrderManualMapper,
    private val subOrderDao: SubOrderDao,
    private val paymentService: PaymentService,
    private val clientSystemDao: ClientSystemDao,
    private val shortLinksIntegration: ShortLinksIntegration,
    private val paymentApiProperties: PaymentApiProperties,
    private val queueStatusResultNameNormalizeService: QueueStatusResultNameNormalizeService,
) : OrderService {
    override fun createOrderInternal(command: CreateOrderCommand): CreateOrderResult {
        val skipSendingErrorsQueue =
            clientSystemDao
                .findBySystemCode(command.clientId)
                ?.skipSendingErrorsQueue
                ?: false

        val order =
            orderManualMapper.toOrderEntity(command, skipSendingErrorsQueue).apply {
                versionApi = command.versionApi
            }

        var savedOrder = orderDao.save(order)

        if (command.versionApi == ApiVersionEnum.V2) {
            enrichWithShortLink(savedOrder)
        }

        val subOrders = orderManualMapper.toSubOrderEntities(savedOrder, command.subOrders)
        subOrders.forEach(savedOrder::addSubOrder)
        subOrderDao.saveAll(subOrders)
        savedOrder = orderDao.save(savedOrder)

        return savedOrder.toCreateOrderResult()
    }

    override fun getOrderStatus(orderId: UUID): DataGetOrderStatus {
        val order = findOrderByIdOrThrow(orderId)
        return DataGetOrderStatus(order.status.desc)
    }

    private fun OrderEntity.toCreateOrderResult(): CreateOrderResult {
        val id = requireNotNull(orderId)
        return CreateOrderResult(
            orderId = id,
            paymentUrl = "${paymentApiProperties.paymentHost}${paymentApiProperties.paymentUrlSuffix}$id",
            shortPaymentUrl = urlPayPageShort,
        )
    }

    override fun payCard(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val order = findOrderByIdOrThrow(orderId)
        checkOrderStatus(order)
        return paymentService.payCard(order, payQueryParams)
    }

    override fun paySbp(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val order = findOrderByIdOrThrow(orderId)
        checkOrderStatus(order)
        return paymentService.paySbp(order, payQueryParams)
    }

    override fun createRegestryOrder(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): OrderEntity =
        OrderEntity(
            paymentEndDate = Instant.now().plus(4, ChronoUnit.HOURS),
            premiumAmount = BigDecimal("1"),
            unifiedId = unifiedId,
            urlToReturn = payQueryParams.urlToReturnS.toString(),
            urlToDecline = payQueryParams.urlToReturnF.toString(),
            saveCard = true,
            regCard = true,
            skipSendingReceipt = true,
            skipSendingQueue = false,
            queueStatusResultName =
                queueStatusResultNameNormalizeService.buildQueueStatusResultName(
                    ORDER_STATUS_PATTERN,
                    clientId,
                ),
            bank = BankEnum.GPB.name,
            clientId = clientId,
            recipientEmail = "",
            recipientPhone = "",
            typePaymentOperation = OperationTypeEnum.REGISTRATION.name,
        ).run(orderDao::save)

    private fun findOrderByIdOrThrow(orderId: UUID): OrderEntity =
        orderDao.findById(orderId) ?: throw BusinessException(ERROR_CODE_ORDER_NOT_FOUND)

    private fun checkOrderStatus(order: OrderEntity): Unit =
        when {
            order.status.isPaidFor() -> throw BusinessException(ERROR_CODE_ORDER_ALREADY_PAID)
            order.status.isAvailable().not() -> throw BusinessException(ERROR_CODE_ORDER_CLOSED)
            else -> {}
        }

    private fun calculateExpireDays(paymentEndDate: Instant?): Int {
        val now = Instant.now()

        val days = ChronoUnit.DAYS.between(now, paymentEndDate)

        return days.coerceAtLeast(1).coerceAtMost(90).toInt()
    }

    private fun enrichWithShortLink(order: OrderEntity) {
        val longUrl =
            "${paymentApiProperties.pagepayinfoHost}${paymentApiProperties.pagepayinfoUrlSuffix}${order.orderId}"

        val expireDays = calculateExpireDays(order.paymentEndDate)

        val request =
            ShortLinkRequest().apply {
                longUrl(longUrl)
                maxVisits(100)
                expireDays(expireDays)
            }

        val shortLink = shortLinksIntegration.createShortLink(request)
        order.urlPayPageShort = shortLink?.data?.shortLink
    }
}
