package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.InvoiceAccountData
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.dto.response.QrBankingDetails
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.net.URI

@Mapper
interface PaymentMethodsMapper {
    @Mapping(source = "order.orderId", target = "orderId")
    @Mapping(source = "order.subOrders", target = "accounts")
    fun toDataOrderPaymentPageInfo(
        order: OrderEntity,
        urlPayBank: URI?,
        paySbp: PaySbp?,
        qrBankingDetails: QrBankingDetails?,
    ): DataOrderPaymentPageInfo

    @Mapping(source = "order.orderId", target = "invoiceId")
    @Mapping(source = "order.subOrders", target = "accounts")
    fun toInvoicePayPageInfo(
        order: OrderEntity,
        urlPayBank: URI?,
        paySbp: PaySbp?,
        qrBankingDetails: QrBankingDetails?,
    ): InvoicePayPageInfo

    @Mapping(source = "contractNumber", target = "agreementNumber")
    @Mapping(source = "premiumAmount", target = "agreementPrice")
    @Mapping(source = "typeInsurance", target = "insuranceKind")
    fun toInvoiceAccountData(subOrder: SubOrderEntity): InvoiceAccountData
}
