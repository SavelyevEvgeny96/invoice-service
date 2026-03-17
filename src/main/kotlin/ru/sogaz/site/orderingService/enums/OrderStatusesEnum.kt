package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

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
        }
    }

    fun isPaidFor() = this == SUCCESS

    fun isAvailable() = this in listOf(NEW, UPDATE)
}
