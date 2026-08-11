package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.response.InvoiceReversalStatusEvent
import ru.sogaz.site.orderingService.dto.response.InvoiceStatusEvent
import ru.sogaz.site.orderingService.dto.response.SubInvoiceData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper
interface InvoiceStatusMapper {
    @Mapping(target = "invoiceId", source = "order.orderId")
    @Mapping(target = "externalSystemCode", source = "order.clientId")
    @Mapping(target = "externalId", source = "order.subscriptionId")
    @Mapping(target = "email", source = "order.recipientEmail")
    @Mapping(target = "status", source = "completedPaymentData.status")
    @Mapping(target = "invoices", source = "order.subOrders")
    @Mapping(target = "keyCard", source = "completedPaymentData.card.cardId")
    @Mapping(target = "issuerName", source = "completedPaymentData.card.issuerName")
    @Mapping(target = "maskedPan", source = "completedPaymentData.card.maskedPan")
    @Mapping(target = "paymentSystem", source = "completedPaymentData.card.paymentSystem")
    @Mapping(target = "title", source = "completedPaymentData.card.title")
    @Mapping(target = "bank", source = "completedPaymentData.bank")
    @Mapping(target = "paymentType", source = "completedPaymentData.paymentType")
    @Mapping(target = "paySucces", source = "completedPaymentData.payDate")
    @Mapping(target = "paymentBankId", source = "completedPaymentData.paymentBankId")
    fun toInvoiceStatusEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): InvoiceStatusEvent

    @Mapping(target = "invoiceId", source = "completedPaymentData.orderId")
    @Mapping(target = "premium", source = "completedPaymentData.totalAmount")
    @Mapping(target = "status", source = "completedPaymentData.status")
    @Mapping(target = "errorText", source = "completedPaymentData.errorText")
    fun toInvoiceReversalStatusEvent(completedPaymentData: CompletedPaymentData): InvoiceReversalStatusEvent

    @Mapping(target = "premium", source = "premiumAmount")
    @Mapping(target = "insuranceKind", source = "typeInsurance")
    @Mapping(target = "agreementId", source = "contractId")
    @Mapping(target = "agreementNumber", source = "contractNumber")
    @Mapping(target = "program", source = "insuranceProgram")
    @Mapping(target = "agreementDate", source = "contractDate")
    fun toInvoiceData(subOrder: SubOrderEntity): SubInvoiceData
}
