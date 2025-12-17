package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.dto.request.SubOrderRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.math.BigDecimal

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
)
abstract class OrderMapper {
    @Mapping(target = "paymentEndDate", source = "orderEndDate")
    @Mapping(target = "recipientEmail", source = "recipientEmail", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "recipientPhone", source = "recipientPhone", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "premiumAmount", source = "subOrders", qualifiedByName = ["mapPremium"])
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createDate", expression = "java(Instant.now())")
    @Mapping(target = "clientId", source = "metaInfo", qualifiedByName = ["mapClientId"])
    abstract fun toOrderEntity(dto: OrderPayloadDto): OrderEntity

    @Mapping(target = "orderEntity", source = "order")
    @Mapping(target = "premiumAmount", source = "dto.premiumAmountDto")
    @Mapping(target = "createDate", expression = "java(Instant.now())")
    abstract fun toSubOrderEntity(
        dto: SubOrderDto,
        order: OrderEntity,
    ): SubOrderEntity

    @Mapping(
        target = "paymentEndDate",
        source = "orderEndDate",
    )
    @Mapping(
        target = "recurrent",
        expression = "java(orderRequest.getOrderIdRecurrent() != null)",
    )
    abstract fun fromRequestDto(orderRequest: OrderRequest): OrderEntity

    abstract fun fromRequestDto(subOrderRequest: SubOrderRequest): SubOrderEntity

    // ---------- Helpers ----------
    @Named("nullToEmpty")
    fun nullToEmpty(value: String?): String = value ?: ""

    @Named("mapClientId")
    fun mapClientId(metaInfo: List<MetaInfoOrder>): String? = metaInfo.firstOrNull()?.author

    @Named("mapPremium")
    fun mapPremium(subOrders: List<SubOrderDto>?): BigDecimal? =
        subOrders
            ?.map { it.premiumAmountDto }
            ?.fold(BigDecimal.ZERO, BigDecimal::add)
            ?.takeIf { it > BigDecimal.ZERO }
}
