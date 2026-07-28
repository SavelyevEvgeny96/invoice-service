package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.PaymentQrData
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
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneOffset.UTC)

        @JvmStatic
        @Named("amountInKopecks")
        fun amountInKopecks(amount: BigDecimal?): String =
            amount
                ?.movePointRight(2)
                ?.toBigIntegerExact()
                ?.toString()
                .orEmpty()

        @JvmStatic
        @Named("lastName")
        fun lastName(policyholder: String?): String = policyholder.parts().getOrElse(0) { "" }

        @JvmStatic
        @Named("firstName")
        fun firstName(policyholder: String?): String = policyholder.parts().getOrElse(1) { "" }

        @JvmStatic
        @Named("middleName")
        fun middleName(policyholder: String?): String = policyholder.parts().drop(2).joinToString(" ")

        @JvmStatic
        @Named("date")
        fun date(value: Instant?): String = value?.let(DATE_FORMATTER::format).orEmpty()

        private fun String?.parts(): List<String> =
            this
                ?.trim()
                ?.split(Regex("\\s+"))
                ?.filter(String::isNotBlank)
                .orEmpty()
    }
}
