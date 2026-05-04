package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.response.OverdueInvoiceRegEvent
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.time.Instant

@Mapper(componentModel = "spring")
abstract class OverdueInvoiceRegMapper {
    @Mappings(
        Mapping(target = "metaInfo", source = "orderEntity", qualifiedByName = ["mapMeta"]),
        Mapping(target = "channel", source = "orderEntity.subOrders", qualifiedByName = ["mapChannel"]),
        Mapping(target = "unifiedId", source = "orderEntity.unifiedId"),
        Mapping(target = "status", constant = "OVERDUE"),
        Mapping(target = "errorText", constant = "Срок жизни счета для регистрации карты истек, карта не была привязана"),
    )
    abstract fun toOverdueInvoiceRegEvent(orderEntity: OrderEntity): OverdueInvoiceRegEvent

    @Named("mapMeta")
    fun mapMeta(order: OrderEntity): List<MetaInfoOrder> =
        listOf(
            MetaInfoOrder(
                eventTimeIso = Instant.now(),
                author = "invoice-service",
                routingKey = order.queueStatusResultName ?: "",
            ),
        )

    @Named("mapChannel")
    fun mapChannel(subOrders: List<SubOrderEntity>?): String = subOrders?.firstOrNull()?.channel ?: ""
}
