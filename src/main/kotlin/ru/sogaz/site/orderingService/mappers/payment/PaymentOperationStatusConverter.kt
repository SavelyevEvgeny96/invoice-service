package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum

@Mapper
abstract class PaymentOperationStatusConverter {
    fun convertStatus(completedPaymentData: CompletedPaymentData): OrderStatusesEnum =
        when {
            completedPaymentData.operationType == OperationTypeEnum.REVERSAL -> OrderStatusesEnum.REVERSAL

            else -> OrderStatusesEnum.SUCCESS
        }
}
