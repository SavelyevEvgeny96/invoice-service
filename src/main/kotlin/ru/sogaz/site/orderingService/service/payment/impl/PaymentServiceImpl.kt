package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapper
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.payment.client.api.PayV2Api

@Service
class PaymentServiceImpl(
    private val paymentServiceMapper: PaymentServiceMapper,
    private val payClient: PayV2Api,
) : PaymentService {
    companion object {
        private const val ERROR_WHILE_GET_PAYMENT_PAGE = "Не удалось получить платежную страницу"
    }

    private val logger = loggerFor(javaClass)

    override fun payCard(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val payRequest = paymentServiceMapper.orderToCardPayRequest(order, payQueryParams)
        val dataPay =
            runCatching { payClient.pay(payRequest) }.getOrNull()?.data ?: throw InnerException(getTraceId(), ERROR_WHILE_GET_PAYMENT_PAGE)
        return paymentServiceMapper.dataPayToPaymentPage(dataPay)
    }

    override fun paySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val payRequest = paymentServiceMapper.orderToSbpPayRequest(order, payQueryParams)
        val dataPay =
            runCatching { payClient.paySbp(payRequest) }.getOrNull()?.data
                ?: throw InnerException(getTraceId(), ERROR_WHILE_GET_PAYMENT_PAGE)
        return paymentServiceMapper.dataPayToPaymentPage(dataPay)
    }

    override fun payQrSbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaySbp? {
        val payRequest = paymentServiceMapper.orderToSbpPayRequest(order, payQueryParams)
        val dataPay = runCatching { payClient.payQrImageSbp(payRequest) }.getOrNull()?.data
        return paymentServiceMapper.dataQrPayToPaySbp(dataPay)
    }
}
