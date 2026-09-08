package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.response.DataOrderPaymentPageInfo
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse
import ru.sogaz.site.orderingService.dto.response.InvoiceAccountData
import ru.sogaz.site.orderingService.dto.response.InvoicePayPageInfo
import ru.sogaz.site.orderingService.dto.response.PaySbp
import ru.sogaz.site.orderingService.dto.response.PaymentMethodDto
import ru.sogaz.site.orderingService.dto.response.QrBankingDetails
import ru.sogaz.site.orderingService.dto.response.SavedCardGid
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
    @Mapping(source = "saveCardRespLk.gidId", target = "gidId")
    @Mapping(source = "saveCardRespLk.paymentMethods", target = "listSavedCardsGid")
    fun toInvoicePayPageInfo(
        saveCardRespLk: GidAuthResponse?,
        gidPayUrl: String?,
        order: OrderEntity,
        urlPayBank: URI?,
        paySbp: PaySbp?,
        qrBankingDetails: QrBankingDetails?,
    ): InvoicePayPageInfo

    @Mapping(source = "id", target = "keyCard")
    @Mapping(source = "details.lastDigits", target = "lastDigits")
    @Mapping(source = "paymentSystem.name", target = "paymentSystem")
    fun toSavedCardGid(method: PaymentMethodDto): SavedCardGid

    @Mapping(source = "contractNumber", target = "agreementNumber")
    @Mapping(source = "premiumAmount", target = "agreementPrice")
    @Mapping(source = "typeInsurance", target = "insuranceKind")
    fun toInvoiceAccountData(subOrder: SubOrderEntity): InvoiceAccountData
}
