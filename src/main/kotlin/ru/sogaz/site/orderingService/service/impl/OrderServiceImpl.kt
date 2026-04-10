package ru.sogaz.site.orderingService.service.impl
import org.springframework.beans.factory.annotation.Value
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
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.service.OrderService
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.shortLinks.ShortLinksIntegration
import ru.sogaz.site.shortlinks.client.model.ShortLinkRequest
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
    @param:Value("\${api.payment.hostNameApp}")
    private val hostNameApp: String,
    @param:Value("\${api.payment.paymentUrlSuffix}")
    private val paymentUrlSuffix: String,
    @param:Value("\${api.payment.paymentUrl}")
    private val payBasePath: String,
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

        savedOrder = orderDao.save(savedOrder)

        val subOrders = orderManualMapper.toSubOrderEntities(savedOrder, command.subOrders)
        subOrders.forEach(savedOrder::addSubOrder)
        subOrderDao.saveAll(subOrders)

        return savedOrder.toCreateOrderResult(payBasePath)
    }

    override fun getOrderStatus(orderId: UUID): DataGetOrderStatus {
        val order = findOrderByIdOrThrow(orderId)
        return DataGetOrderStatus(order.status.desc)
    }

    private fun OrderEntity.toCreateOrderResult(basePath: String): CreateOrderResult {
        val id = requireNotNull(orderId)
        return CreateOrderResult(
            orderId = id,
            paymentUrl = "$basePath$id",
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

        return days.coerceAtLeast(1).coerceAtMost(60).toInt()
    }

    private fun enrichWithShortLink(order: OrderEntity) {
        val longUrl = "$hostNameApp$paymentUrlSuffix${order.orderId}"

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
