package ru.sogaz.site.orderingService.mappers.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest.TypeEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest

@Mapper(
    uses = [ReceiptTotalAmountMapper::class],
    imports = [TypeEnum::class],
)
abstract class ReceiptPaymentMapper {
    @Mapping(target = "type", constant = "_1")
    @Mapping(target = "sum", source = "totalAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    abstract fun mapFromPaymentToPaymentRequest(completedPaymentData: CompletedPaymentData): PaymentPaymentRequest

    fun mapFromPaymentToListOfPaymentRequests(completedPaymentData: CompletedPaymentData): List<PaymentPaymentRequest> =
        listOf(mapFromPaymentToPaymentRequest(completedPaymentData))

    fun mapPaymentType(operationType: OperationTypeEnum): PaymentReceiptCreateRequest.ReceiptTypeEnum =
        when (operationType) {
            OperationTypeEnum.REFUND -> PaymentReceiptCreateRequest.ReceiptTypeEnum.SELL_REFUND
            else -> PaymentReceiptCreateRequest.ReceiptTypeEnum.SELL
        }
}
