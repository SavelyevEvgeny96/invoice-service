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
        const val CODE_OK_SUCCESS_GET_LIST_ORDER = 1101570200
    }

    override fun findOrders(request: OrdersUserRequest): Response<OrdersUserResponse> {
        logger.info(GET_ORDER_INFO_REQUEST.format(request.searchName, request.status))

        // --- 1. Поиск ордеров по параметрам ---
        val foundOrders =
            when (request.searchName) {
                OrdersUserSearchNameEnum.USER_ID -> request.userId?.let { orderDao.findByRecipientUserId(it) }
                OrdersUserSearchNameEnum.GD_ID -> request.gdId?.let { orderDao.findByRecipientGdId(it) }
                OrdersUserSearchNameEnum.EMAIL_OR_PHONE ->
                    when (request.condition) {
                        OrdersUserConditionEnum.OR -> orderDao.findByEmailOrPhone(request.email, request.phone)
                        OrdersUserConditionEnum.AND ->
                            request.email?.let { email ->
                                request.phone?.let { phone ->
                                    orderDao.findByEmailAndPhone(email, phone)
                                }
                            }
                        else -> emptyList()
                    }
                else -> emptyList()
            }.takeIf { it?.isNotEmpty() == true } ?: run {
                logger.error(CLIENT_NOT_FOUND)
                throw BusinessException(ERROR_CODE_GET_LIST_ORDER, getTraceId())
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
                else -> emptyList()
            }.takeIf { it.isNotEmpty() } ?: run {
                logger.error(STATUS_NOT_FOUND.format(request.status))
                throw BusinessException(ERROR_CODE_GET_LIST_ORDER_STATUS_NOT_FOUND, getTraceId())
            }// --- 3. Добавление subOrders ---
        val enrichedOrders = filtered.mapNotNull { order ->
            val subOrders = subOrderDao.findByOrderId((order?.orderId ?: "").toString())
                .mapNotNull { sub ->
                    sub?.let {
                        SubOrderItem(
                            policyId = it.policyId,
                            policyNumber = it.policyNumber,
                            typeInsurance = it.typeInsurance,
                            premiumAmount = it.premiumAmount
                        )
                    }
                }

            order?.let {
                OrderItem(
                    orderId = it.orderId,
                    premiumAmount = it.premiumAmount,
                    subOrdersList = subOrders
                )
            }
        }

        // --- 4. Формирование ответа ---
        logger.info(COUNT_GET_ORDER.format(enrichedOrders.size))
        return getSuccessResponse(getTraceId(), CODE_OK_SUCCESS_GET_LIST_ORDER, OrdersUserResponse(enrichedOrders))
    }
}