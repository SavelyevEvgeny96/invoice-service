package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.math.BigDecimal

class PremiumAmountValidator : ConstraintValidator<ValidPremiumAmount, BigDecimal?> {
    override fun isValid(
        value: BigDecimal?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) return false

        if (value <= BigDecimal.ZERO) return false

        // Отсекает значения вида 1E+3, которые могли попасть как BigDecimal с отрицательным scale
        if (value.scale() < 0) return false

        return true
    }
}
