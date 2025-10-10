package ru.sogaz.site.orderingService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrderItem
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.site.orderingService.enums.*
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

class OrdersUserServiceImpl(
    private val orderDao: OrderDao
) : OrdersUserService {
    private val logger = loggerFor(OrdersUserServiceImpl::class.java)
    companion object {
        const val STATUS_NOT_FOUND = "order с указанным status=%s не найден"
        const val COUNT_GET_ORDER = "общее число order получено=%d"
        const val GET_ORDER_INFO_REQUEST = "Получение списка ордеров: ПО searchName=%s, СО status=%s"
        const val CLIENT_NOT_FOUND = "Ошибка получения списка заказов клиента. Клиент с такими данными не найден"
    }

    override fun findOrders(request: OrdersUserRequest): Response<OrdersUserResponse> {
        logger.info(GET_ORDER_INFO_REQUEST.format(request.searchName, request.status))
        val foundOrders = when (request.searchName) {
            OrdersUserSearchNameEnum.USER_ID -> request.userId?.let { orderDao.findByRecipientUserId(it) }
            OrdersUserSearchNameEnum.GD_ID -> request.gdId?.let { orderDao.findByRecipientGdId(it) }
            OrdersUserSearchNameEnum.EMAIL_OR_PHONE -> when (request.condition) {
                OrdersUserConditionEnum.OR -> orderDao.findByEmailOrPhone(request.email, request.phone)
                OrdersUserConditionEnum.AND -> request.email?.let {
                    request.phone?.let { it1 ->
                        orderDao.findByEmailAndPhone(it, it1)
                    }
                }
                else -> emptyList()
            }
            else -> emptyList()
        }.takeIf { it?.isNotEmpty() == true } ?: run {
            logger.error(CLIENT_NOT_FOUND)
            throw InnerException(getTraceId(), CLIENT_NOT_FOUND)
            //            BusinessException(CODE_CLIENT_NOT_FOUND, getTraceId(), MSG_CLIENT_NOT_FOUND)
        }

        val filtered = when (request.status) {
            OrdersStatusIsPaidEnum.UNPAID -> foundOrders.filter {
                it?.status in setOf(
                    OrderStatusesEnum.NEW,
                    OrderStatusesEnum.UPDATE
                )
            }

            OrdersStatusIsPaidEnum.PAID -> foundOrders.filter { it?.status?.isPaidFor() == true }
            else -> emptyList()
        }.takeIf { it.isNotEmpty() } ?: run {
            logger.error(STATUS_NOT_FOUND.format(request.status))
            throw InnerException(getTraceId(), STATUS_NOT_FOUND.format(request.status))
//            BusinessException(CODE_ORDERS_BY_STATUS_NOT_FOUND, getTraceId(), MSG_ORDERS_BY_STATUS_NOT_FOUND)
        }
        val result = OrdersUserResponse(
            ordersList = filtered.mapNotNull { it?.let { e -> OrderItem(e.orderId, e.premiumAmount) } }
        )
        logger.info(COUNT_GET_ORDER.format(result.ordersList.size))
        return getSuccessResponse(getTraceId(), 200, result)
    }

}