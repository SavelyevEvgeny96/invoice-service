package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PremiumAmountValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPremiumAmount(
    val message: String = "{validation.orderRequest.premium.invalid}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
