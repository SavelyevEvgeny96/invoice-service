package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.request.InvoicePayCardGidRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.payment.client.model.BankPaymentPageData
import java.time.Instant

@Mapper
interface PaymentOperationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "bank", source = "order.bank")
    @Mapping(target = "operation", constant = "PAY")
    @Mapping(target = "type", constant = "CARD_GID")
    @Mapping(target = "amount", source = "order.premiumAmount")
    @Mapping(target = "depersonalization", source = "request.depersonalization")
    @Mapping(target = "paymentBankId", source = "paymentData.paymentBankId")
    @Mapping(target = "pan", ignore = true)
    @Mapping(target = "paymentSystem", ignore = true)
    @Mapping(target = "payDate", source = "operationTime")
    @Mapping(target = "payerIp", source = "request.payerIP")
    @Mapping(target = "externalErrorCode", ignore = true)
    @Mapping(target = "errorText", ignore = true)
    @Mapping(target = "updateDate", source = "operationTime")
    @Mapping(target = "orderEntity", source = "order")
    fun fromGidPayment(
        order: OrderEntity,
        request: InvoicePayCardGidRequest,
        paymentData: BankPaymentPageData,
        operationTime: Instant,
    ): PaymentOperationEntity

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", source = "status")
    @Mapping(target = "type", source = "paymentType")
    @Mapping(target = "operation", source = "operationType")
    @Mapping(target = "amount", source = "totalAmount")
    @Mapping(target = "pan", source = "card.maskedPan")
    @Mapping(target = "paymentSystem", source = "card.paymentSystem")
    fun fromCompletedPayment(completedPaymentData: CompletedPaymentData): PaymentOperationEntity
}
