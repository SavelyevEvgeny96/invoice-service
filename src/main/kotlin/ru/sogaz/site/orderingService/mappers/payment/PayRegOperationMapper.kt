package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.payment.client.model.PayRegOperationRequest

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
)
interface PayRegOperationMapper {
    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "amount", source = "order.premiumAmount")
    @Mapping(target = "description", constant = "Регистрация карты")
    @Mapping(target = "depersonalization", source = "params.depersonalization")
    @Mapping(target = "saveCard", constant = "true")
    @Mapping(target = "payItems", ignore = true)
    @Mapping(target = "params", source = "params")
    fun mapToRequest(
        order: OrderEntity,
        params: PayQueryParams,
    ): PayRegOperationRequest
}
