package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RussianNameValidator::class])
annotation class RussianNameValid(
    val message: String = "name must contain 2-30 characters, only Russian letters, spaces and hyphens",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class RussianNameValidator : ConstraintValidator<RussianNameValid, String> {
    private val nameRegex = Regex("^[а-яА-ЯёЁ\\s-]{2,30}$")

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = value.isNullOrBlank() || nameRegex.matches(value)
}
