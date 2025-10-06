package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

/** Статус запрошенных заказов */
enum class OrdersStatusIsPaid(@JsonValue val value: String) {
    UNPAID("unpaid"),
    PAID("paid"),
    ALL("all");

    companion object {
        @JvmStatic @JsonCreator
        fun from(value: String?): OrdersStatusIsPaid? {
            if (value.isNullOrBlank()) return null
            val traceId =getTraceId()
            return entries.find { it.value == value }
                ?: throw InnerException(traceId, "Invalid OrdersStatus: '$value'")
        }
    }
}
