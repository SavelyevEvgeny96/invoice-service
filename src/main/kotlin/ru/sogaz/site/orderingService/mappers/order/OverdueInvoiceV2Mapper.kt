package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.OverdueInvoiceV2Event
import ru.sogaz.site.orderingService.dto.response.OverdueSubInvoiceV2Dto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper(componentModel = "spring")
interface OverdueInvoiceV2Mapper {
    @Mapping(target = "invoiceId", source = "orderId")
    @Mapping(target = "externalSystemCode", source = "clientId")
    @Mapping(target = "externalId", source = "subscriptionId")
    @Mapping(target = "status", constant = "OVERDUE")
    @Mapping(target = "errorText", constant = "Срок жизни счета истек, счет не был оплачен")
    @Mapping(target = "invoices", source = "subOrders")
    @Mapping(target = "email", source = "recipientEmail")
    fun toEvent(order: OrderEntity): OverdueInvoiceV2Event

    @Mapping(target = "premium", source = "premiumAmount")
    @Mapping(target = "agreementId", source = "contractId")
    @Mapping(target = "agreementNumber", source = "contractNumber")
    @Mapping(target = "agreementDate", source = "contractDate")
    @Mapping(target = "insuranceKind", source = "typeInsurance")
    fun toInvoiceData(subOrder: SubOrderEntity): OverdueSubInvoiceV2Dto
}
