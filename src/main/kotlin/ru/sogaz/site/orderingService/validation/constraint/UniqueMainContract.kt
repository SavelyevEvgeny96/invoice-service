package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.dto.request.SubOrderRequest
import ru.sogaz.site.orderingService.dto.request.HasMainContractCheck
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueMainContractValidator::class])
annotation class UniqueMainContract(
    val message: String = "Not unique main contract",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class UniqueMainContractValidator : ConstraintValidator<UniqueMainContract, Collection<HasMainContractCheck>> {
    override fun isValid(
        orders: Collection<HasMainContractCheck>,
        context: ConstraintValidatorContext?,
    ): Boolean = orders.count { it.mainContractCheck } <= 1
}