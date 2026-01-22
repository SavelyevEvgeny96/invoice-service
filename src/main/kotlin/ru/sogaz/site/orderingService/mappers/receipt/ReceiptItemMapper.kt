package ru.sogaz.site.orderingService.mappers.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.payment.receipt.client.model.PaymentItemRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentItemRequest.PaymentMethodEnum

@Mapper(
    uses = [ReceiptTotalAmountMapper::class],
    imports = [PaymentMethodEnum::class],
)
interface ReceiptItemMapper {
    companion object {
        private const val RECEIPT_CONTRACT_NUMBER = "Страховая премия по договору №"

        @JvmStatic
        @Named("mapItemName")
        fun mapItemName(subOrder: SubOrderEntity): String = "$RECEIPT_CONTRACT_NUMBER${subOrder.contractNumber}"
    }

    @Mapping(target = "name", source = ".", qualifiedByName = ["mapItemName"])
    @Mapping(target = "sum", source = "premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "price", source = "premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "paymentObject", constant = "service")
    @Mapping(target = "paymentMethod", constant = "FULL_PAYMENT")
    @Mapping(target = "quantity", constant = "1.0")
    @Mapping(target = "vatType", constant = "NONE")
    fun fromSubOrder(subOrder: SubOrderEntity): PaymentItemRequest
}
