package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_ALREADY_PAID
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CLOSED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dto.request.InvoicePayCardGidRequest
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.payment.PaymentOperationMapper
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapper
import ru.sogaz.site.orderingService.service.payment.InvoicePayCardGidService
import ru.sogaz.site.payment.client.api.PayV2Api
import java.time.Instant
import java.util.UUID

@Service
class InvoicePayCardGidServiceImpl(
    private val orderDao: OrderDao,
    private val paymentOperationDao: PaymentOperationDao,
    private val paymentOperationMapper: PaymentOperationMapper,
    private val paymentServiceMapper: PaymentServiceMapper,
    private val payClient: PayV2Api,
) : InvoicePayCardGidService {
    companion object {
        private const val PAYMENT_SYSTEM_UNAVAILABLE = "Ошибка совершения платежа. Платежная система недоступна"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun createPayment(
        invoiceId: UUID,
        request: InvoicePayCardGidRequest,
    ): PaymentPage {
        logger.info("Регистрация оплаты через ГИД для счета {}", invoiceId)
        val order = orderDao.findById(invoiceId) ?: throw BusinessException(ERROR_CODE_ORDER_NOT_FOUND)

        when {
            order.status.isPaidFor() -> throw BusinessException(ERROR_CODE_ORDER_ALREADY_PAID)
            !order.status.isAvailable() -> throw BusinessException(ERROR_CODE_ORDER_CLOSED)
        }

        // Фиксируем начало новой попытки оплаты до обращения к эквайрингу.
        order.updateDate = Instant.now()
        orderDao.save(order)

        val paymentRequest = paymentServiceMapper.orderToGidPayRequest(order, request)
        val response =
            runCatching { payClient.payGid(paymentRequest) }
                .onFailure { logger.error("Payment-service не зарегистрировал оплату через ГИД для счета {}", invoiceId, it) }
                .getOrElse { throw InnerException(getTraceId(), PAYMENT_SYSTEM_UNAVAILABLE) }
        val paymentData = response.data ?: throw InnerException(getTraceId(), PAYMENT_SYSTEM_UNAVAILABLE)

        val paymentOperation = paymentOperationMapper.fromGidPayment(order, request, paymentData, Instant.now())
        paymentOperationDao.save(paymentOperation)

        logger.info("Оплата через ГИД для счета {} зарегистрирована", invoiceId)
        return paymentServiceMapper.dataPayToPaymentPage(paymentData)
    }
}
