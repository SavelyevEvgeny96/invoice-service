package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.mappers.order.InvoiceMetaInfoMapper
import ru.sogaz.site.orderingService.service.order.OrderPaymentPageService
import ru.sogaz.site.orderingService.service.payment.PaymentPageInfoService
import java.util.UUID

@Service
@Transactional
class OrderPaymentPageServiceImpl(
    private val orderDao: OrderDao,
    private val paymentPageInfoService: PaymentPageInfoService,
    private val invoiceMetaInfoMapper: InvoiceMetaInfoMapper,
) : OrderPaymentPageService {
    override fun getPaymentPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        if (order.status.isAvailable().not()) {
            throw BusinessException(CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO)
        }
        return paymentPageInfoService.getInfo(order, payQueryParams)
    }

    override fun getMetaInfo(orderId: UUID): InvoiceMetaInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        return invoiceMetaInfoMapper.toInvoiceMetaInfo(order)
    }
}
