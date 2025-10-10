package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

/** Статус запрошенных заказов */
enum class OrdersStatusIsPaidEnum(
    @JsonValue val values: String,
) {
    UNPAID("unpaid"),
    PAID("paid"),
    ALL("all"),
    ;

    companion object {
        @JvmStatic @JsonCreator
        fun from(value: String?): OrdersStatusIsPaidEnum? {
            if (value.isNullOrBlank()) return null
            val traceId = getTraceId()
            return entries.find { it.values == value }
                ?: throw InnerException(traceId, "Invalid OrdersStatus: '$value'")
        }
    }
}
