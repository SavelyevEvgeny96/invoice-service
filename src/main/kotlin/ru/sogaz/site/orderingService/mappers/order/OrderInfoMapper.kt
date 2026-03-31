package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.CompletedPaymentInfo
import ru.sogaz.site.orderingService.dto.response.SentReceiptInfo
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.entity.ReceiptEntity

@Mapper
interface OrderInfoMapper {
    @Mapping(target = "invoiceId", source = "payment.orderEntity.orderId")
    @Mapping(target = "payerIP", source = "payment.payerIp")
    @Mapping(target = "paymentDate", source = "payment.payDate")
    @Mapping(target = "paymentMethod", source = "payment.type")
    @Mapping(target = "cardNumber", source = "payment.pan")
    @Mapping(target = "listReceipts", source = "receipts")
    fun mapToCompletedOperationInfo(
        payment: PaymentOperationEntity?,
        receipts: List<ReceiptEntity>,
    ): CompletedPaymentInfo?

    @Mapping(target = "receiptId", source = "id")
    @Mapping(target = "receiptSendingStatus", source = "state")
    fun mapReceipt(receipt: ReceiptEntity): SentReceiptInfo
}
