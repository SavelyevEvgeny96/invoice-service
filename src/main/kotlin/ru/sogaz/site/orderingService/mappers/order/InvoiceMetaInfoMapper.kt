package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaAccount
import ru.sogaz.site.orderingService.dto.response.InvoiceMetaInfo
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper
interface InvoiceMetaInfoMapper {
    @Mapping(source = "orderId", target = "invoiceId")
    @Mapping(source = "paymentEndDate", target = "invoiceEndDate")
    @Mapping(source = "subscriptionId", target = "externalId")
    @Mapping(source = "recipientEmail", target = "email")
    @Mapping(source = "status", target = "invoiceStatus")
    @Mapping(source = "subOrders", target = "accounts")
    fun toInvoiceMetaInfo(orderEntity: OrderEntity): InvoiceMetaInfo

    @Mapping(source = "contractNumber", target = "agreementNumber")
    @Mapping(source = "contractDate", target = "agreementDate")
    @Mapping(source = "typeInsurance", target = "insuranceKind")
    @Mapping(source = "insuranceProgram", target = "program")
    fun toInvoiceMetaAccounts(subOrder: SubOrderEntity): InvoiceMetaAccount
}
