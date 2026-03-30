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
    @Mapping(source = "orders", target = "subOrders")
    fun toCommand(request: OrderRequestV1): CreateOrderCommand

    fun toCommand(request: SubOrderRequestV1): CreateSubOrderCommand

    @Mapping(source = "invoices", target = "subOrders")
    @Mapping(source = "email", target = "recipientEmail")
    @Mapping(source = "phoneNumber", target = "recipientPhone")
    @Mapping(source = "invoiceEndDate", target = "orderEndDate")
    fun toCommand(request: OrderRequestV2): CreateOrderCommand

    @Mapping(constant = "false", target = "mainContractCheck")
    @Mapping(source = "premium", target = "premiumAmount")
    @Mapping(source = "agreementNumber", target = "contractNumber")
    @Mapping(source = "agreementId", target = "contractId")
    @Mapping(source = "agreementDate", target = "contractDate")
    @Mapping(source = "insuranceKind", target = "typeInsurance")
    @Mapping(source = "program", target = "insuranceProgram")
    fun toCommand(request: SubOrderRequestV2): CreateSubOrderCommand
}
