package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.PaymentQrData
import ru.sogaz.site.orderingService.dto.response.FileQR
import ru.sogaz.site.orderingService.dto.response.InvoicePaymentQr
import ru.sogaz.site.orderingService.entity.CompanyDetailsQrEntity
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper(componentModel = "spring")
interface PaymentQrMapper {
    @Mapping(target = "name", source = "company.name")
    @Mapping(target = "personalAcc", source = "company.personalAcc")
    @Mapping(target = "bankName", source = "company.bankName")
    @Mapping(target = "bic", source = "company.bic")
    @Mapping(target = "correspAcc", source = "company.correspAcc")
    @Mapping(target = "payeeInn", source = "company.payeeInn")
    @Mapping(target = "kpp", source = "company.kpp")
    @Mapping(target = "sum", source = "order.premiumAmount", qualifiedByName = ["amountInKopecks"])
    @Mapping(target = "lastName", source = "order.payerLastName", defaultValue = "")
    @Mapping(target = "firstName", source = "order.payerFirstName", defaultValue = "")
    @Mapping(target = "middleName", source = "order.payerMiddleName", defaultValue = "")
    @Mapping(target = "contractNumber", source = "subOrder.contractNumber", defaultValue = "")
    @Mapping(target = "contractDate", source = "subOrder.contractDate", qualifiedByName = ["date"])
    fun toPaymentQrData(
        order: OrderEntity,
        subOrder: SubOrderEntity?,
        company: CompanyDetailsQrEntity,
    ): PaymentQrData

    @Mapping(target = "qrInfo.contentQR", source = "qr.content")
    @Mapping(target = "qrInfo.mediaType", source = "qr.mediaType")
    @Mapping(target = "detais.payerFio.lastName", source = "order.payerLastName", defaultValue = "")
    @Mapping(target = "detais.payerFio.firstName", source = "order.payerFirstName", defaultValue = "")
    @Mapping(target = "detais.payerFio.middleName", source = "order.payerMiddleName", defaultValue = "")
    @Mapping(target = "detais.recipient.name", source = "company.name")
    @Mapping(target = "detais.recipient.inn", source = "company.payeeInn")
    @Mapping(target = "detais.recipient.kpp", source = "company.kpp")
    @Mapping(target = "detais.recipient.personalAcc", source = "company.personalAcc")
    @Mapping(target = "detais.recipient.purposePayment", constant = PAYMENT_PURPOSE)
    @Mapping(target = "detais.bank.name", source = "company.bankName")
    @Mapping(target = "detais.bank.bic", source = "company.bic")
    @Mapping(target = "detais.bank.corresAcc", source = "company.correspAcc")
    @Mapping(target = "infoInvoice.premiumAmount", source = "order.premiumAmount", qualifiedByName = ["amount"])
    @Mapping(target = "infoInvoice.accounts.agreementNumber", source = "subOrder.contractNumber", defaultValue = "")
    @Mapping(target = "infoInvoice.accounts.insuranceKind", source = "subOrder.typeInsurance", defaultValue = "")
    @Mapping(target = "infoInvoice.accounts.agreementPrice", source = "subOrder.premiumAmount", qualifiedByName = ["amount"])
    @Mapping(target = "infoInvoice.urlSuccess", source = "order.urlToReturn", defaultValue = "")
    fun toInvoicePaymentQr(
        order: OrderEntity,
        subOrder: SubOrderEntity?,
        company: CompanyDetailsQrEntity,
        qr: FileQR,
    ): InvoicePaymentQr

    companion object {
        const val PAYMENT_PURPOSE = "СТРАХОВОЙ ВЗНОС ПО ДОГОВОРУ СТРАХОВАНИЯ"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneOffset.UTC)

        @JvmStatic
        @Named("amount")
        fun amount(amount: BigDecimal?): String = amount?.toPlainString().orEmpty()

        @JvmStatic
        @Named("amountInKopecks")
        fun amountInKopecks(amount: BigDecimal?): String =
            amount
                ?.movePointRight(2)
                ?.toBigIntegerExact()
                ?.toString()
                .orEmpty()

        @JvmStatic
        @Named("date")
        fun date(value: Instant?): String = value?.let(DATE_FORMATTER::format).orEmpty()
    }
}
