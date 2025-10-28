package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.math.BigDecimal

@Mapper(componentModel = "spring")
abstract class OrderMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "urlToReturn", ignore = true)
    @Mapping(target = "urlToDecline", ignore = true)
    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "paymentType", ignore = true)
    @Mapping(target = "status", expression = "java(ru.sogaz.site.orderingService.enums.OrderStatusesEnum.NEW)")
    @Mapping(target = "recurrent", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "unifiedId", source = "unifiedId")
    @Mapping(target = "policyholder", source = "policyholder")
    @Mapping(target = "subscriptionId", source = "subscriptionId")
    @Mapping(target = "keyCard", source = "keyCard")
    @Mapping(target = "saveCard", source = "saveCard")
    @Mapping(target = "paymentEndDate", source = "orderEndDate")
    @Mapping(target = "recipientEmail", source = "recipientEmail", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "recipientPhone", source = "recipientPhone", qualifiedByName = ["nullToEmpty"])
    @Mapping(target = "recipientUserId", source = "recipientUserId")
    @Mapping(target = "premiumAmount", source = "subOrders", qualifiedByName = ["mapPremium"])
    @Mapping(target = "createDate", expression = "java(java.time.Instant.now())")
    abstract fun toOrderEntity(dto: OrderPayloadDto): OrderEntity

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderEntity", source = "order")
    @Mapping(target = "policyId", source = "dto.policyId")
    @Mapping(target = "policyNumber", source = "dto.policyNumber")
    @Mapping(target = "contractId", source = "dto.contractId")
    @Mapping(target = "contractNumber", source = "dto.contractNumber")
    @Mapping(target = "docType", source = "dto.docType")
    @Mapping(target = "channel", source = "dto.channel")
    @Mapping(target = "mainContractCheck", source = "dto.mainContractCheck")
    @Mapping(target = "insuranceProgram", source = "dto.insuranceProgram")
    @Mapping(target = "typeInsurance", source = "dto.typeInsurance")
    @Mapping(target = "premiumAmount", source = "dto.premiumAmountDto")
    @Mapping(target = "createDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "contractDate", source = "dto.contractDate")
    @Mapping(target = "policyDate", source = "dto.policyDate")
    abstract fun toSubOrderEntity(
        dto: SubOrderDto,
        order: OrderEntity,
    ): SubOrderEntity

    // ---------- Helpers ----------
    @Named("nullToEmpty")
    fun nullToEmpty(value: String?): String = value ?: ""

    @Named("mapPremium")
    fun mapPremium(subOrders: List<SubOrderDto>?): BigDecimal? =
        subOrders
            ?.map { it.premiumAmountDto }
            ?.fold(BigDecimal.ZERO, BigDecimal::add)
            ?.takeIf { it > BigDecimal.ZERO }
}
