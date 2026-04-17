package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity

@Mapper
interface PaymentOperationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", source = "status")
    @Mapping(target = "type", source = "paymentType")
    @Mapping(target = "operation", source = "operationType")
    @Mapping(target = "amount", source = "totalAmount")
    @Mapping(target = "pan", source = "card.maskedPan")
    @Mapping(target = "paymentSystem", source = "card.paymentSystem")
    fun fromCompletedPayment(completedPaymentData: CompletedPaymentData): PaymentOperationEntity
}
