package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_REGISTRY_CARD_NOT_AVAILABLE_INFO
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.service.order.OrderService
import ru.sogaz.site.orderingService.service.payment.CardRegistryService
import ru.sogaz.site.orderingService.service.subOrder.SubOrderService
import ru.sogaz.site.payment.client.model.DataPay
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.orThrow
import ru.sogaz.site.paymentService.service.CardRegistryService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.SubOrderService
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl
import kotlin.let

@Service
class CardRegistryServiceImpl(
    private val orderService: OrderService,
    private val subOrderService: SubOrderService
) : CardRegistryService {
    @Transactional
    override fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): DataPay {
        val order: OrderEntity = createRegistryOrder(unifiedId, payQueryParams, clientId)
        return order.orderId

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
