package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

enum class OrderStatusesEnum(
    @JsonValue val desc: String,
) {
    NEW("NEW"),
    UPDATE("UPDATE"),
    OVERDUE("OVERDUE"),
    MARKEDDEL("MARKEDDEL"),
    SUCCESS("SUCCESS"),
    CANCELED("CANCELED"),
    REFUND("REFUND"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): OrderStatusesEnum? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.desc.equals(value, ignoreCase = true) }
                ?: throw InnerException(getTraceId(), "Invalid OrderStatus: '$value'")
        }
    }

    fun isPaidFor() = this == SUCCESS

    fun isAvailable() = this in listOf(NEW, UPDATE)
}
