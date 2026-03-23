package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.CreateSubOrderCommand
import ru.sogaz.site.orderingService.dto.request.OrderRequestV1
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.dto.request.SubOrderRequestV1
import ru.sogaz.site.orderingService.dto.request.SubOrderRequestV2

@Mapper(componentModel = "spring")
interface OrderRequestCommandMapper {
    @Mapping(target = "typePaymentOperation", constant = "")
    fun toCommand(request: OrderRequestV1): CreateOrderCommand

    @Mapping(target = "mainContractCheck", source = "mainContractCheck")
    fun toCommand(request: SubOrderRequestV1): CreateSubOrderCommand

    fun toCommand(request: OrderRequestV2): CreateOrderCommand

    @Mapping(target = "mainContractCheck", constant = "false")
    fun toCommand(request: SubOrderRequestV2): CreateSubOrderCommand
}
