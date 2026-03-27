package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.dto.data.PaidOrderMessage
import ru.sogaz.site.orderingService.dto.data.SubOrderPayload
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper
interface PaidOrderMessagesMapper {
    companion object {
        @JvmStatic
        @Named("instantToFormattedString")
        fun instantToFormattedString(dateTime: Instant?): String? =
            dateTime
                ?.atZone(ZoneOffset.UTC)
                ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        @JvmStatic
        @Named("instantToEpochMilli")
        fun instantToEpochMilli(dateTime: Instant?): Long? = dateTime?.toEpochMilli()

        @JvmStatic
        @Named("statusIfRecurrent")
        fun statusIfRecurrent(order: OrderEntity): String? = if (order.recurrent == true) order.status.desc else null
    }

    @Mapping(
        target = "paySuccess",
        source = "completedPaymentData.payDate",
        qualifiedByName = ["instantToFormattedString"],
    )
    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "externalSystemCode", source = "order.clientId")
    @Mapping(target = "status", source = "completedPaymentData.status")
    @Mapping(target = "bank", source = "completedPaymentData.bank")
    @Mapping(target = "paymentType", source = "completedPaymentData.paymentType")
    @Mapping(target = "keyCard", source = "completedPaymentData.card.cardId")
    @Mapping(target = "issuerName", source = "completedPaymentData.card.issuerName")
    @Mapping(target = "maskedPan", source = "completedPaymentData.card.maskedPan")
    @Mapping(target = "paymentSystem", source = "completedPaymentData.card.paymentSystem")
    fun toPaidOrderMessage(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ): PaidOrderMessage

    @Mapping(target = "contractDate", qualifiedByName = ["instantToEpochMilli"])
    @Mapping(target = "policyDate", qualifiedByName = ["instantToEpochMilli"])
    fun toSubOrderPayload(subOrderEntity: SubOrderEntity): SubOrderPayload
}
