package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaAccount
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaPayment
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class InvoiceMetaInfoMapper {
    @Mapping(source = "orderId", target = "invoiceId")
    @Mapping(source = "paymentEndDate", target = "invoiceEndDate")
    @Mapping(source = "subscriptionId", target = "externalId")
    @Mapping(source = "recipientEmail", target = "email")
    @Mapping(source = "status", target = "invoiceStatus")
    @Mapping(source = "subOrders", target = "accounts")
    @Mapping(target = "payment", ignore = true)
    abstract fun toInvoiceMetaInfo(orderEntity: OrderEntity): InvoiceMetaInfo

    @Mapping(source = "contractNumber", target = "agreementNumber")
    @Mapping(source = "contractDate", target = "agreementDate")
    @Mapping(source = "premiumAmount", target = "agreementPrice")
    @Mapping(source = "typeInsurance", target = "insuranceKind")
    @Mapping(source = "insuranceProgram", target = "program")
    abstract fun toInvoiceMetaAccounts(subOrder: SubOrderEntity): InvoiceMetaAccount

    @Mapping(source = "state", target = "status", qualifiedByName = ["mapPaymentStatus"])
    @Mapping(source = "bank", target = "bank", qualifiedByName = ["mapBank"])
    abstract fun toInvoiceMetaPayment(paymentOperation: PaymentOperationEntity): InvoiceMetaPayment

    @Named("mapPaymentStatus")
    fun mapPaymentStatus(state: PaymentOperationStateEnum?): String =
        if (state == PaymentOperationStateEnum.SUCCESS) {
            PaymentOperationStateEnum.SUCCESS.name
        } else {
            PaymentOperationStateEnum.FAIL.name
        }

    @Named("mapBank")
    fun mapBank(bank: BankEnum?): String? = bank?.name
}
