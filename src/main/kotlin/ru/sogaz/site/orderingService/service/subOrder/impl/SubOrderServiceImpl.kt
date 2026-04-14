package ru.sogaz.site.orderingService.service.subOrder.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_SUB_ORDER_CROSS_SELL
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.subOrder.SubOrderService
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.SubOrderMapper
import ru.sogaz.site.paymentService.service.SubOrderService
import java.math.BigDecimal
import kotlin.collections.first
import kotlin.jvm.javaClass
import kotlin.run

@Service
class SubOrderServiceImpl(
    private val subOrderDao: SubOrderDao
) : SubOrderService {
    companion object {
        const val DOC_TYPE_REGISTRY_CARD = "Регистрация карты"
    }

    override fun createSuborder(
        order: OrderEntity,
        clientId: String,
    ): SubOrderEntity =
        SubOrderEntity(
            orderEntity = order,
            docType = DOC_TYPE_REGISTRY_CARD,
            premiumAmount = order.premiumAmount,
            mainContractCheck = true,
            channel = clientId,
        ).run(subOrderDao::save)
}
