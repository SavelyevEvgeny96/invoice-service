package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OperationTypeEnum

@Mapper(
    imports = [OperationTypeEnum::class],
    uses = [PaymentOperationStatusConverter::class],
)
interface CompletedPaymentMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "keyCard", source = "card.cardId")
    @Mapping(target = "status", source = ".")
    @Mapping(
        target = "refundDate",
        source = "payDate",
        conditionExpression = "java(completedPaymentData.getOperationType().equals(OperationTypeEnum.REFUND))",
    )
    fun fillPaidOrder(
        @MappingTarget order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): OrderEntity
}
