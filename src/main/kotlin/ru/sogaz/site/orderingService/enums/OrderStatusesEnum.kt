package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

enum class OrderStatusesEnum(
    @JsonValue val values: String,
) {
    NEW("NEW"),
    UPDATE("UPDATE"),
    OVERDUE("OVERDUE"),
    MARKEDDEL("MARKEDDEL"),
    SUCCESS("SUCCESS"),
    CANCELED("CANCELED"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): OrderStatusesEnum? {
            if (value.isNullOrBlank()) return null
            val traceId = getTraceId()
            return entries.find { it.values.equals(value, ignoreCase = true) }
                ?: throw InnerException(traceId, "Invalid OrderStatus: '$value'")
        }
    }

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this in listOf(OVERDUE, MARKEDDEL)
}
