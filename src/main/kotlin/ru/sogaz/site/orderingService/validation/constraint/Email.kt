package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EmailValidator::class])
annotation class Email(
    val message: String = "invalid email",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EmailValidator(
    private val regex: Regex,
) : ConstraintValidator<Email, String?> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = value?.matches(regex) ?: false
}
