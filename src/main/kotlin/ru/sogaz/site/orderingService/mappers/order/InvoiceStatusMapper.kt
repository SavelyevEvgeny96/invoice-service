package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
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
    fun toInvoiceStatusEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): InvoiceStatusEvent

    @Mapping(target = "premium", source = "premiumAmount")
    @Mapping(target = "insuranceKind", source = "insuranceProgram")
    fun toInvoiceData(subOrder: SubOrderEntity): SubInvoiceData
}
