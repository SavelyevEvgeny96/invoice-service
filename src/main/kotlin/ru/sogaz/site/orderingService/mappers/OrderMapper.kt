package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.request.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.math.BigDecimal
import java.util.UUID

@Mapper
abstract class OrderMapper {
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = ["stringToUUID"])
    @Mapping(target = "recipientEmail", source = "recipientEmail", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "recipientPhone", source = "recipientPhone", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "paymentEndDate", source = "orderEndDate")
    @Mapping(target = "createDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "premiumAmount", source = "subOrders", qualifiedByName = ["mapPremium"])
    abstract fun toOrderEntity(dto: OrderPayloadDto): OrderEntity

    @Mapping(target = "orderEntity", source = "order")
    @Mapping(target = "managerEmail", source = "managerEmail")
    abstract fun toSubOrderEntity(
        dto: SubOrderDto,
        order: OrderEntity,
        managerEmail: String?,
    ): SubOrderEntity

    // ----------------- Helpers -----------------
    @Named("stringToUUID")
    fun stringToUUID(orderId: String): UUID = UUID.fromString(orderId)

    @Named("nullToEmpty")
    fun nullToEmpty(value: String?): String = value ?: ""

    @Named("mapPremium")
    fun mapPremium(subOrders: List<SubOrderDto>?): BigDecimal? =
        subOrders
            ?.map { it.premiumAmountDto }
            ?.fold(BigDecimal.ZERO, BigDecimal::add)
            ?.takeIf { it > BigDecimal.ZERO }
}
