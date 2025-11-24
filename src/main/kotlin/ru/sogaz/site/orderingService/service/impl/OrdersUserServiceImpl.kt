package ru.sogaz.site.orderingService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_GET_LIST_ORDER
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_GET_LIST_ORDER_STATUS_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrderItem
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.site.orderingService.dto.response.SubOrderItem
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.OrdersStatusIsPaidEnum
import ru.sogaz.site.orderingService.enums.OrdersUserConditionEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchNameEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

class OrdersUserServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
) : OrdersUserService {
    private val logger = loggerFor(OrdersUserServiceImpl::class.java)

    companion object {
        const val STATUS_NOT_FOUND = "order с указанным status=%s не найден"
        const val COUNT_GET_ORDER = "общее число order получено=%d"
        const val GET_ORDER_INFO_REQUEST = "Получение списка ордеров: ПО searchName=%s, СО status=%s"
        const val CLIENT_NOT_FOUND = "Ошибка получения списка заказов клиента. Клиент с такими данными не найден"
        const val ERROR_GET_ORDERS_EXCEPTION = "Ошибка при получении списка ордеров [%s]: %s"
        const val ERROR_GET_SUBORDERS_EXCEPTION = "Ошибка при получении subOrders для orderId=%s [%s]: %s"
        const val CODE_OK_SUCCESS_GET_LIST_ORDER = 1101570200
    }

    override fun findOrders(request: OrdersUserRequest): Response<OrdersUserResponse> {
        logger.info(GET_ORDER_INFO_REQUEST.format(request.searchName, request.status))

        // --- 1. Поиск ордеров по параметрам ---
        val foundOrders: List<OrderEntity?> =
            try {
                when (request.searchName) {
                    OrdersUserSearchNameEnum.USER_ID ->
                        request.userId?.let { orderDao.findByRecipientUserId(it) }

                    OrdersUserSearchNameEnum.GD_ID ->
                        request.gdId?.let { orderDao.findByRecipientGdId(it) }

                    OrdersUserSearchNameEnum.EMAIL_OR_PHONE ->
                        when (request.condition) {
                            OrdersUserConditionEnum.OR ->
                                orderDao.findByEmailOrPhone(request.email, request.phone)

                            OrdersUserConditionEnum.AND ->
                                if (!request.email.isNullOrBlank() && !request.phone.isNullOrBlank()) {
                                    orderDao.findByEmailAndPhone(request.email!!, request.phone!!)
                                } else {
                                    emptyList()
                                }

                            else -> emptyList()
                        }

                    else -> emptyList()
                } ?: emptyList()
            } catch (ex: Exception) {
                logger.error(ERROR_GET_ORDERS_EXCEPTION.format(ex::class.simpleName, ex.message), ex)
                throw BusinessException(
                    ERROR_CODE_GET_LIST_ORDER,
                    getTraceId(),
                )
            }

        // Проверка: если не найдено ни одного заказа
        if (foundOrders.isEmpty()) {
            logger.error(CLIENT_NOT_FOUND)
            throw BusinessException(ERROR_CODE_GET_LIST_ORDER, getTraceId(), "error")
        }
        // --- 2. Фильтрация по статусу ---
        val filtered =
            when (request.status) {
                OrdersStatusIsPaidEnum.UNPAID ->
                    foundOrders.filter {
                        it?.status in setOf(OrderStatusesEnum.NEW, OrderStatusesEnum.UPDATE)
                    }

                OrdersStatusIsPaidEnum.PAID ->
                    foundOrders.filter { it?.status?.isPaidFor() == true }

                OrdersStatusIsPaidEnum.ALL -> foundOrders.filter {
                    it?.status in setOf(
                        OrderStatusesEnum.NEW, OrderStatusesEnum.UPDATE,
                        OrderStatusesEnum.CANCELED, OrderStatusesEnum.SUCCESS,
                        OrderStatusesEnum.MARKEDDEL, OrderStatusesEnum.OVERDUE
                    )
                }
                else -> emptyList()
            }.takeIf { it.isNotEmpty() } ?: run {
                logger.error(STATUS_NOT_FOUND.format(request.status))
                throw BusinessException(ERROR_CODE_GET_LIST_ORDER_STATUS_NOT_FOUND, getTraceId())
            }

        // --- 3. Добавление subOrders ---
        val enrichedOrders =
            filtered.mapNotNull { order ->
                val subOrders =
                    try {
                        subOrderDao
                            .findByOrderId(order?.orderId)
                            .mapNotNull { sub ->
                                sub?.let {
                                    SubOrderItem(
                                        policyId = it.policyId,
                                        policyNumber = it.policyNumber,
                                        typeInsurance = it.typeInsurance,
                                        premiumAmount = it.premiumAmount,
                                    )
                                }
                            }
                    } catch (ex: Exception) {
                        logger.error(
                            ERROR_GET_SUBORDERS_EXCEPTION.format(order?.orderId, ex::class.simpleName, ex.message),
                            ex,
                        )
                        emptyList()
                    }

                order?.let {
                    OrderItem(
                        it.orderId,
                        it.premiumAmount,
                        subOrdersList = subOrders,
                        status = it.status.values,
                    )
                }
            }

        // --- 4. Формирование ответа ---
        logger.info(COUNT_GET_ORDER.format(enrichedOrders.size))
        return getSuccessResponse(getTraceId(), CODE_OK_SUCCESS_GET_LIST_ORDER, OrdersUserResponse(enrichedOrders))
    }
}
