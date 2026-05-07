package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.dto.data.RefundReversalPaymentDto
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.time.ZoneId

/**
 * Маппер DTO для ответа и команды возврата.
 */
@Mapper(componentModel = "spring")
abstract class OrderRefundMapper {
    companion object {
        private const val REFUND_DESCRIPTION_EMPTY = "Отмена транзакции по договору"
        private const val REFUND_DESCRIPTION_PATTERN = "Отмена транзакции по договору №%s"
        private const val REFUND_DATE_SUFFIX = " от %s"
    }

    @Mapping(target = "status", constant = "error")
    @Mapping(target = "errorText", source = "errorText")
    abstract fun toErrorDto(
        invoiceId: java.util.UUID,
        errorText: String,
    ): RefundResponseDto

    @Mapping(target = "description", expression = "java(buildRefundDescription(subOrder))")
    abstract fun toReversalPaymentDto(
        payment: PaymentOperationEntity,
        subOrder: SubOrderEntity?,
    ): RefundReversalPaymentDto

    protected fun buildRefundDescription(subOrder: SubOrderEntity?): String {
        val contractNumber = subOrder?.contractNumber
        if (contractNumber.isNullOrBlank()) return REFUND_DESCRIPTION_EMPTY

        val base = REFUND_DESCRIPTION_PATTERN.format(contractNumber)
        val localDate = subOrder?.contractDate?.atZone(ZoneId.systemDefault())?.toLocalDate()
        return if (localDate == null) base else base + REFUND_DATE_SUFFIX.format(localDate)
    }
}
