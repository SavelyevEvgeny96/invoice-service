package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CANCELED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_OVERDUE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_SUCCESS
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.CANCELED
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.MARKEDDEL
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.OVERDUE
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.REFUND
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.SUCCESS
import ru.sogaz.site.orderingService.mappers.order.InvoiceMetaInfoMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentMethodsMapper
import ru.sogaz.site.orderingService.service.order.OrderPaymentPageService
import ru.sogaz.site.orderingService.service.payment.PayInfoService
import java.util.UUID

@Service
@Transactional
class OrderPaymentPageServiceImpl(
    private val orderDao: OrderDao,
    private val payInfoService: PayInfoService,
    private val paymentMethodsMapper: PaymentMethodsMapper,
    private val invoiceMetaInfoMapper: InvoiceMetaInfoMapper,
) : OrderPaymentPageService {
    override fun getInvoicePayPageInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): InvoicePayPageInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        order.checkStatus()
        val (payCardUri, paySbp) = payInfoService.getInfo(order, payQueryParams)
        return paymentMethodsMapper.toInvoicePayPageInfo(order, payCardUri, paySbp)
    }

    override fun getPaymentPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        order.checkStatus()
        val (payCardUri, paySbp) = payInfoService.getInfo(order, payQueryParams)
        return paymentMethodsMapper.toDataOrderPaymentPageInfo(order, payCardUri, paySbp)
    }

    override fun getMetaInfo(orderId: UUID): InvoiceMetaInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        return invoiceMetaInfoMapper.toInvoiceMetaInfo(order)
    }

    private fun OrderEntity.checkStatus() {
        when(status) {
            SUCCESS -> throw BusinessException(ERROR_CODE_ORDER_SUCCESS)
            OVERDUE,
            MARKEDDEL -> throw BusinessException(ERROR_CODE_ORDER_OVERDUE)
            CANCELED,
            REFUND -> throw BusinessException(ERROR_CODE_ORDER_CANCELED)
            else -> {}
        }
    }
}
