package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.response.InvoiceStatusRegEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper(componentModel = "spring")
abstract class OverdueInvoiceRegMapper {
    @Mappings(
        Mapping(target = "channel", source = "orderEntity.subOrders", qualifiedByName = ["mapChannel"]),
        Mapping(target = "unifiedId", source = "orderEntity.unifiedId"),
        Mapping(target = "status", constant = "OVERDUE"),
        Mapping(target = "errorText", constant = "Срок жизни счета для регистрации карты истек, карта не была привязана"),
    )
    abstract fun toOverdueInvoiceRegEvent(orderEntity: OrderEntity): InvoiceStatusRegEvent

    @Named("mapChannel")
    fun mapChannel(subOrders: List<SubOrderEntity>?): String = subOrders?.firstOrNull()?.channel ?: ""
}
