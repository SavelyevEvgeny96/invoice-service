package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.payment.PaymentServiceMapper
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.payment.client.api.PayV2Api

@Service
class PaymentServiceImpl(
    private val paymentServiceMapper: PaymentServiceMapper,
    private val payClient: PayV2Api,
) : PaymentService {
    override fun payCard(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val payRequest = paymentServiceMapper.orderToPayRequest(order)
        val dataPay =
            payClient.pay(
                payRequest,
                payQueryParams.urlToReturn.toString(),
                payQueryParams.urlToReturnS.toString(),
                payQueryParams.urlToReturnF.toString(),
                payQueryParams.depersonalization,
            )
        return paymentServiceMapper.dataPayToPaymentPage(dataPay)
    }

    override fun paySbp(
        order: OrderEntity,
        payQueryParams: PayQueryParams,
    ): PaymentPage {
        val payRequest = paymentServiceMapper.orderToPayRequest(order)
        val dataPay =
            payClient.paySbp(
                payRequest,
                payQueryParams.urlToReturn.toString(),
                payQueryParams.urlToReturnS.toString(),
                payQueryParams.urlToReturnF.toString(),
                payQueryParams.depersonalization,
            )
        return paymentServiceMapper.dataPayToPaymentPage(dataPay)
    }
}
