package ru.sogaz.site.orderingService.mappers.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.ReceiptTypeEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.SystemEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.VersionEnum

@Mapper(
    uses = [ReceiptTotalAmountMapper::class, ReceiptItemMapper::class, ReceiptPaymentMapper::class, ReceiptClientInfoMapper::class],
    imports = [ReceiptTypeEnum::class, SystemEnum::class, VersionEnum::class],
)
interface ReceiptMapper {
    @Mapping(target = "client", source = "order")
    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "items", source = "order.subOrders")
    @Mapping(target = "total", source = "order.premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "payments", source = "completedPaymentData")
    @Mapping(target = "depersonalization", source = "completedPaymentData.depersonalization")
    @Mapping(target = "receiptType", source = "completedPaymentData.operationType")
    @Mapping(target = "system", constant = "ATOL")
    @Mapping(target = "version", constant = "V4")
    fun mapFromPaymentToReceiptCreateRequest(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): PaymentReceiptCreateRequest
}
