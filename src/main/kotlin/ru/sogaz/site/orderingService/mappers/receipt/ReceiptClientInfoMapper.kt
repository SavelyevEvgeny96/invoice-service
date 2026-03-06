package ru.sogaz.site.orderingService.mappers.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.payment.receipt.client.model.ClientInfo

@Mapper
interface ReceiptClientInfoMapper {
    @Mapping(target = "email", source = "recipientEmail")
    @Mapping(target = "phone", source = "recipientPhone")
    @Mapping(target = "name", source = "policyholder")
    fun fromOrder(order: OrderEntity): ClientInfo
}
