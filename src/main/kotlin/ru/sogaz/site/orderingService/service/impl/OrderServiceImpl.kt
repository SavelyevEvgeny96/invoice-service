package ru.sogaz.site.orderingService.service.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.properties.ServiceStatuses
import ru.sogaz.site.orderingService.service.OrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

@Service
class OrderServiceImpl(
    private val orderDao: OrderDao,
    private val clientSystemDao: ClientSystemDao,
    private val orderManualMapper: OrderManualMapper,
    @Value("\${api.payment.paymentUrl}")
    private val payBasePath: String,
) : OrderService {
    /**
     * Метод для создания заказа.
     * @param orderRequest Данные о заказе(содержит внутри лист subOrderRequest)
     * @throws Exception Если данные невалидны или произошла ошибка при сохранении
     * @return Объект DataOrder, содержащий информацию о платежном запросе
     */
    @Transactional
    override fun createOrder(orderRequest: OrderRequest): Response<DataOrder> {
        val skipSendingErrorsQueue =
            clientSystemDao
                .findBySystemCode(orderRequest.clientId)
                ?.skipSendingErrorsQueue
                ?: false

        val order = orderManualMapper.toOrderEntity(orderRequest, skipSendingErrorsQueue)
        val savedOrder = orderDao.save(order)

        return getSuccessResponse(
            RequestInfo.getTraceId(),
            ServiceStatuses.STATUS_CODE_SUCCESS,
            savedOrder.toDataOrder(payBasePath),
        )
    }

    private fun OrderEntity.toDataOrder(basePath: String): DataOrder {
        val id = requireNotNull(orderId)
        return DataOrder(id, "$basePath$id")
    }
}
