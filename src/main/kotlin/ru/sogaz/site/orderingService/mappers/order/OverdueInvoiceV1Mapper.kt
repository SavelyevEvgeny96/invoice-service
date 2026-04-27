package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.OverdueInvoiceV1Event
import ru.sogaz.site.orderingService.dto.response.OverdueSubOrderV1Dto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity

@Mapper(componentModel = "spring")
interface OverdueInvoiceV1Mapper {
    @Mapping(target = "externalSystemCode", source = "clientId")
    @Mapping(target = "status", constant = "error")
    @Mapping(target = "errorText", constant = "Срок жизни счета истек, счет не был оплачен")
    @Mapping(target = "subOrders", source = "subOrders")
    fun toEvent(order: OrderEntity): OverdueInvoiceV1Event

    fun toSubOrder(subOrder: SubOrderEntity): OverdueSubOrderV1Dto
}
