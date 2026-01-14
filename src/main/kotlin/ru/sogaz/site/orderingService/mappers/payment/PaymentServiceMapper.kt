package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.payment.client.model.DataPay
import ru.sogaz.site.payment.client.model.PayRequest

@Mapper(componentModel = "spring", uses = [ContractInfoMapper::class])
interface PaymentServiceMapper {
    @Mapping(target = "amount", source = "premiumAmount")
    @Mapping(target = "contractsInfo", source = "subOrders")
    fun orderToPayRequest(order: OrderEntity): PayRequest

    @Mapping(target = "uri", source = "paymentPageUrl")
    fun dataPayToPaymentPage(dataPay: DataPay): PaymentPage
}
