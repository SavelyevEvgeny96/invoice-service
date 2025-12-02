package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

/** Логическое условие при одновременной передаче email и phone */
enum class OrdersUserConditionEnum(
    @JsonValue val values: String,
) {
    OR("or"),
    AND("and"),
    ;

    companion object {
        @JvmStatic @JsonCreator
        fun from(value: String?): OrdersUserConditionEnum? {
            if (value.isNullOrBlank()) return null
            val traceId = getTraceId()
            return entries.find { it.values.equals(value, ignoreCase = true) }
                ?: throw InnerException(traceId, "Invalid Condition: '$value'")
        }
    }
}
