package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.dto.data.RefundReversalPaymentDto
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity

/**
 * Маппер DTO для ответа и команды возврата.
 */
@Mapper(componentModel = "spring")
interface OrderRefundMapper {
    @Mapping(target = "status", constant = "error")
    @Mapping(target = "errorText", source = "errorText")
    fun toErrorDto(
        invoiceId: java.util.UUID,
        errorText: String,
    ): RefundResponseDto

    fun toReversalPaymentDto(
        payment: PaymentOperationEntity,
        description: String,
    ): RefundReversalPaymentDto
}
