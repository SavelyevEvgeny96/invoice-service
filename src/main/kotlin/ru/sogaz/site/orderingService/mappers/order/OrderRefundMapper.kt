package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.entity.OrderEntity

@Mapper(
    imports = [ArrayList::class],
)
interface OrderRefundMapper {
    @Mapping(target = "premiumAmount", source = "order.premiumAmount")
    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "status", source = "completedPaymentData.status")
    @Mapping(target = "metaInfo", expression = "java( new ArrayList<MetaInfoOrder>() )")
    fun toRefundResponseDto(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): RefundResponseDto
}
