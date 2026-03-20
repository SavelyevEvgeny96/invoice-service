package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.net.URI

@Mapper
interface PaymentMethodsMapper {
    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "accounts", source = "order.subOrders")
    fun toDataOrderPaymentPageInfo(
        order: OrderEntity,
        urlPayBank: URI,
        paySbp: PaySbp?,
    ): DataOrderPaymentPageInfo
}
