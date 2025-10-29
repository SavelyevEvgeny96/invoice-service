package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId

/** Поле, по которому ищем */
enum class OrdersUserSearchNameEnum(
    @JsonValue val values: String,
) {
    USER_ID("userId"),
    UNIFIED_ID("unifiedId"),
    EMAIL_OR_PHONE("emailOrPhone"),
    ;

    companion object {
        @JvmStatic @JsonCreator
        fun from(value: String?): OrdersUserSearchNameEnum? {
            if (value.isNullOrBlank()) return null
            val traceId = getTraceId()
            return entries.find { it.values.equals(value, ignoreCase = true) }
                ?: throw InnerException(traceId, "Invalid SearchName: '$value'")
        }
    }
}
