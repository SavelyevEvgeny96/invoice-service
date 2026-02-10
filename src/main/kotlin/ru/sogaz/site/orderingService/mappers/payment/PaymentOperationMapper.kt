package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity

@Mapper
interface PaymentOperationMapper {
    @Mapping(target = "state", source = "completedPaymentData.status")
    @Mapping(target = "type", source = "completedPaymentData.paymentType")
    fun fromCompletedPayment(completedPaymentData: CompletedPaymentData): PaymentOperationEntity
}
