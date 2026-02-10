package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity

@Mapper
interface CompletedPaymentMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "status", constant = "SUCCESS")
    fun fillPaidOrder(
        @MappingTarget order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): OrderEntity
}
