package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrderItem
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.OrdersStatusIsPaidEnum
import ru.sogaz.site.orderingService.enums.OrdersUserConditionEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchNameEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrdersUserService

@Service
class OrdersUserServiceImpl(
    private val orderDao: OrderDao,
) : OrdersUserService {
    private val logger = loggerFor(javaClass)

    override fun findOrders(request: OrdersUserRequest): OrdersUserResponse {
        logger.info(
            "Получение списка ордеров: start, searchName={}, status={}, traceId={}",
            request.searchName,
            request.status,
            getTraceId(),
        )

        val foundOrders =
            when (request.searchName) {
                OrdersUserSearchNameEnum.USER_ID ->
                    orderDao.findByRecipientUserId(requireNotNull(request.userId))

                OrdersUserSearchNameEnum.GD_ID ->
                    orderDao.findByRecipientGdId(requireNotNull(request.gdId))

                OrdersUserSearchNameEnum.EMAIL_OR_PHONE -> {
                    when (request.condition) {
                        OrdersUserConditionEnum.OR ->
                            orderDao.findByEmailOrPhone(request.email, request.phone)

                        OrdersUserConditionEnum.AND ->
                            orderDao.findByEmailAndPhone(
                                requireNotNull(request.email),
                                requireNotNull(request.phone),
                            )
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        if (foundOrders.isEmpty()) {
            logger.warn("findOrders: client not found by search={}, traceId={}", request.searchName, getTraceId())
            // 409 – клиент с такими данными не найден
//            throw BusinessException(CODE_CLIENT_NOT_FOUND, getTraceId(), MSG_CLIENT_NOT_FOUND)
        }
        val filtered =
            when (request.status) {
                OrdersStatusIsPaidEnum.UNPAID ->
                    foundOrders.filter { it!!.status in setOf(OrderStatusesEnum.NEW, OrderStatusesEnum.UPDATE) }
                OrdersStatusIsPaidEnum.PAID ->
                    foundOrders.filter { it!!.status.isPaidFor() }

                else -> emptyList()
            }

        if (filtered.isEmpty()) {
            logger.warn(
                "findOrders: orders by status not found. status={}, traceId={}",
                request.status,
                getTraceId(),
            )
            // 409 – заказы с указанными статусами не найдены
//            throw BusinessException(CODE_ORDERS_BY_STATUS_NOT_FOUND, getTraceId(), MSG_ORDERS_BY_STATUS_NOT_FOUND)
        }

        val orderItems =
            filtered.map { entity ->
                OrderItem(
                    orderId = entity?.orderId,
                    premiumAmount = entity?.premiumAmount, // по требованию — без сабов
                )
            }

        val result = OrdersUserResponse(ordersList = orderItems)

        logger.info(
            "findOrders: success, ordersCount={}, traceId={}",
            result.ordersList.size,
            getTraceId(),
        )
        return result
    }
}
