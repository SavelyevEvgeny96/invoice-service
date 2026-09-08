package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CANCELED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_OVERDUE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_SUCCESS
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.site.orderingService.dto.response.QrBankingDetails
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.CANCELED
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.MARKEDDEL
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.OVERDUE
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.REFUND
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum.SUCCESS
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.enums.PaymentQrBank
import ru.sogaz.site.orderingService.mappers.order.InvoiceMetaInfoMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentMethodsMapper
import ru.sogaz.site.orderingService.service.order.OrderPaymentPageService
import ru.sogaz.site.orderingService.service.payment.PayInfoService
import ru.sogaz.site.orderingService.service.payment.PaymentMethodsResolver
import java.util.UUID

@Service
@Transactional
class OrderPaymentPageServiceImpl(
    private val orderDao: OrderDao,
    private val payInfoService: PayInfoService,
    private val paymentMethodsMapper: PaymentMethodsMapper,
    private val invoiceMetaInfoMapper: InvoiceMetaInfoMapper,
    private val paymentOperationDao: PaymentOperationDao,
    private val paymentMethodsResolver: PaymentMethodsResolver,
) : OrderPaymentPageService {
    override fun getInvoicePayPageInfo(
        invoiceId: UUID,
        payQueryParams: PayQueryParams,
    ): InvoicePayPageInfo {
        val order = orderDao.findById(invoiceId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        order.checkStatus()
        val methods = paymentMethodsResolver.resolve(order)
        val (payCardUri, paySbp) = payInfoService.getInfo(order, payQueryParams, methods)
        val (saveCardRespLk, gidPayUrl) = payInfoService.getGidIdInfo(payQueryParams, invoiceId)
        return paymentMethodsMapper.toInvoicePayPageInfo(
            saveCardRespLk,
            gidPayUrl,
            payQueryParams,
            order,
            payCardUri,
            paySbp,
            order.qrBankingDetails(methods)
        )
    }

    override fun getPaymentPage(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo {
        val order = orderDao.findById(orderId) ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)
        order.checkStatus()
        val methods = paymentMethodsResolver.resolve(order)
        val (payCardUri, paySbp) = payInfoService.getInfo(order, payQueryParams, methods)
        return paymentMethodsMapper.toDataOrderPaymentPageInfo(
            order,
            payCardUri,
            paySbp,
            order.qrBankingDetails(methods)
        )
    }

    override fun getMetaInfo(
        orderId: UUID,
        payment: Boolean,
    ): InvoiceMetaInfo {
        val order =
            orderDao.findById(orderId)
                ?: throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO)

        val metaInfo = invoiceMetaInfoMapper.toInvoiceMetaInfo(order)

        if (!payment) {
            return metaInfo
        }

        val lastPaymentOperation =
            paymentOperationDao.findLastByOrderId(orderId)
                ?: return metaInfo

        return metaInfo.copy(
            payment = invoiceMetaInfoMapper.toInvoiceMetaPayment(lastPaymentOperation),
        )
    }

    private fun OrderEntity.checkStatus() {
        when (status) {
            SUCCESS -> throw BusinessException(ERROR_CODE_ORDER_SUCCESS)
            OVERDUE,
            MARKEDDEL,
                -> throw BusinessException(ERROR_CODE_ORDER_OVERDUE)

            CANCELED,
            REFUND,
                -> throw BusinessException(ERROR_CODE_ORDER_CANCELED)

            else -> {}
        }
    }

    private fun OrderEntity.qrBankingDetails(methods: Set<PaymentMethod>): QrBankingDetails? {
        if (PaymentMethod.QR_BANKING_DETAILS !in methods) return null
        val banks = bankQr
        return QrBankingDetails(
            gpbAvailability = banks.isNullOrEmpty() || PaymentQrBank.GPB in banks,
            vtpAvailability = banks.isNullOrEmpty() || PaymentQrBank.VTB in banks,
        )
    }
}
