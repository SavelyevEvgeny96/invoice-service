package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.payment.client.model.BankPaymentPageData
import ru.sogaz.site.payment.client.model.BankPaymentQrContent
import ru.sogaz.site.payment.client.model.CardPayOperationRequest
import ru.sogaz.site.payment.client.model.CardRecurrentOperationRequest
import ru.sogaz.site.payment.client.model.SbpPayOperationRequest

@Mapper(uses = [PaymentPurposeMapper::class])
interface PaymentServiceMapper {
    @Mapping(target = "amount", source = "order.premiumAmount")
    @Mapping(target = "description", source = "order.subOrders", qualifiedByName = ["mapCardRequestContractDescription"])
    @Mapping(target = "payItems", source = "order.subOrders", qualifiedByName = ["mapRequestParams"])
    @Mapping(target = "depersonalization", source = "params.depersonalization")
    @Mapping(target = "payerIp", source = "params.payerIP")
    @Mapping(target = "params", source = "params")
    @Mapping(
        target = "params.redirectParams",
        expression = "java(paymentPurposeMapper.mapRedirectParams(params, order))",
    )
    fun orderToCardPayRequest(
        order: OrderEntity,
        params: PayQueryParams,
    ): CardPayOperationRequest

    @Mapping(target = "amount", source = "premiumAmount")
    @Mapping(target = "description", source = "subOrders", qualifiedByName = ["mapCardRequestContractDescription"])
    @Mapping(target = "payItems", source = "subOrders", qualifiedByName = ["mapRequestParams"])
    fun orderToCardRecurrentPayRequest(order: OrderEntity): CardRecurrentOperationRequest

    fun ordersToCardRecurrentPayRequests(orders: List<OrderEntity>): List<CardRecurrentOperationRequest>

    @Mapping(target = "amount", source = "order.premiumAmount")
    @Mapping(target = "description", source = "order.subOrders", qualifiedByName = ["mapSbpRequestContractDescription"])
    @Mapping(target = "payerIp", source = "params.payerIP")
    @Mapping(target = "depersonalization", source = "params.depersonalization")
    @Mapping(target = "params", source = "params")
    @Mapping(
        target = "params.redirectParams",
        expression = "java(paymentPurposeMapper.mapRedirectParams(params, order))",
    )
    fun orderToSbpPayRequest(
        order: OrderEntity,
        params: PayQueryParams,
    ): SbpPayOperationRequest

    @Mapping(target = "uri", source = "paymentPageUrl")
    fun dataPayToPaymentPage(bankPaymentPageData: BankPaymentPageData): PaymentPage

    @Mapping(target = "urlPay", source = "paymentPageUrl")
    @Mapping(target = "fileQR", source = "qrImageData")
    fun dataQrPayToPaySbp(bankPaymentQrContent: BankPaymentQrContent?): PaySbp?
}
