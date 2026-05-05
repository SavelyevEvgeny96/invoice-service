package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.response.InvoiceStatusRegEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper(componentModel = "spring")
abstract class InvoiceStatusRegMapper {
    @Mapping(target = "channel", source = "order.subOrders", qualifiedByName = ["mapChannel"])
    @Mapping(target = "unifiedId", source = "order.unifiedId")
    @Mapping(target = "status", source = "completedPaymentData.status")
    @Mapping(target = "errorText", source = "completedPaymentData.errorText")
    @Mapping(target = "keyCard", source = "completedPaymentData.card.cardId")
    @Mapping(target = "maskedPan", source = "completedPaymentData.card.maskedPan")
    @Mapping(target = "title", source = "completedPaymentData.card.title")
    @Mapping(target = "paymentSystem", source = "completedPaymentData.card.paymentSystem")
    @Mapping(target = "issuerName", source = "completedPaymentData.card.issuerName")
    @Mapping(target = "paymentType", source = "completedPaymentData.paymentType")
    @Mapping(target = "bank", source = "completedPaymentData.bank")
    @Mapping(target = "payDate", source = "completedPaymentData.payDate")
    abstract fun toInvoiceStatusRegEvent(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): InvoiceStatusRegEvent

    @Named("mapChannel")
    fun mapChannel(subOrders: List<SubOrderEntity>?): String = subOrders?.firstOrNull()?.channel ?: ""
}
