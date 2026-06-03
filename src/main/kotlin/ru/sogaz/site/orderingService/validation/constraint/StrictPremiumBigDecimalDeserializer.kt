package ru.sogaz.site.orderingService.validation.constraint

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal

class StrictPremiumBigDecimalDeserializer : JsonDeserializer<BigDecimal>() {
    companion object {
        private val PREMIUM_REGEX = Regex("""^\d+(\.\d+)?$""")
    }

    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): BigDecimal {
        val token = parser.currentToken
        val rawValue = parser.text?.trim()

        if (token != JsonToken.VALUE_NUMBER_INT && token != JsonToken.VALUE_NUMBER_FLOAT) {
            return BigDecimal.ZERO
        }

        if (rawValue.isNullOrBlank() || !PREMIUM_REGEX.matches(rawValue)) {
            return BigDecimal.ZERO
        }

        val premium = rawValue.toBigDecimalOrNull() ?: return BigDecimal.ZERO

        if (premium <= BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }

        return premium
    }

    override fun getNullValue(context: DeserializationContext?): BigDecimal = BigDecimal.ZERO
}
