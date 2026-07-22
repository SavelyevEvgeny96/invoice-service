package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValueValidator::class])
annotation class ValidEnum(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "Значение должно соответствовать одному из enum",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EnumValueValidator : ConstraintValidator<ValidEnum, String?> {
    private lateinit var allowedValues: Set<String>

    override fun initialize(annotation: ValidEnum) {
        allowedValues =
            annotation.enumClass.java.enumConstants
                .map { it.name }
                .toSet()
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true
        }

        return allowedValues.contains(value.uppercase())
    }
}
