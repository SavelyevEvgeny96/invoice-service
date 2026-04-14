package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV1
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV2
import ru.sogaz.site.orderingService.dto.response.CreateOrderResult

@Mapper(componentModel = "spring")
interface CreateOrderResponseMapper {
    @Mapping(source = "paymentUrl", target = "url")
    fun toV1(result: CreateOrderResult): CreateOrderDataV1

    @Mapping(source = "orderId", target = "invoiceId")
    @Mapping(source = "paymentUrl", target = "url")
    @Mapping(source = "shortPaymentUrl", target = "urlPayPageShort")
    fun toV2(result: CreateOrderResult): CreateOrderDataV2
}
