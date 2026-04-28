package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.payment.PayRegOperationMapper
import ru.sogaz.site.orderingService.service.order.OrderService
import ru.sogaz.site.orderingService.service.payment.CardRegistryService
import ru.sogaz.site.orderingService.service.subOrder.SubOrderService
import ru.sogaz.site.payment.client.api.CardRegistryV2ControllerApi
import ru.sogaz.site.payment.client.model.BankPaymentPageData

@Service
class CardRegistryServiceImpl(
    private val orderService: OrderService,
    private val subOrderService: SubOrderService,
    private val cardRegistryV2Api: CardRegistryV2ControllerApi,
    private val payRegOperationMapper: PayRegOperationMapper,
) : CardRegistryService {
    @Transactional
    override fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): String {
        val order: OrderEntity = createRegistryOrder(unifiedId, payQueryParams, clientId)
        val payRegOperationRequestMapping = payRegOperationMapper.mapToRequest(order, payQueryParams)
        val response: BankPaymentPageData =
            cardRegistryV2Api.cardRegistry(payRegOperationRequestMapping)
        return response.paymentPageUrl
    }

    private fun createRegistryOrder(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): OrderEntity {
        // создание записи в DB Orders
        val order: OrderEntity = orderService.createRegestryOrder(unifiedId, payQueryParams, clientId)
        // создание записи в DB Suborder
        subOrderService.createSuborder(order, clientId)
        return order
    }
}
