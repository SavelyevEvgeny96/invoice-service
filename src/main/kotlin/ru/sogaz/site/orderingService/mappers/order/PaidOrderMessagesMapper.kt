package ru.sogaz.site.orderingService.mappers.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.PaidOrderMessage
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper
interface PaidOrderMessagesMapper {
    companion object {
        @JvmStatic
        @Named("instantToFormattedString")
        fun instantToFormattedString(dateTime: Instant): String =
            dateTime
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        @JvmStatic
        @Named("statusIfRecurrent")
        fun statusIfRecurrent(order: OrderEntity): String? = if (order.recurrent == true) order.status.desc else null
    }

    @Mapping(target = "externalSystemCode", source = "order.clientId")
    @Mapping(
        target = "paySuccess",
        source = "order.updateDate",
        qualifiedByName = ["instantToFormattedString"],
    )
    @Mapping(target = "status", source = "order", qualifiedByName = ["statusIfRecurrent"])
    fun toPaidOrderMessage(
        order: OrderEntity,
        errorText: String? = null,
    ): PaidOrderMessage
}
