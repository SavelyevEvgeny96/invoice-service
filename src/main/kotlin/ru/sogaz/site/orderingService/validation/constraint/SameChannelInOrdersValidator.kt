package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.orderingService.dto.request.SubOrderRequestV1
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SameChannelInOrdersValidator::class])
annotation class SameChannelInOrders(
    val message: String = "Not unique channel",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class SameChannelInOrdersValidator : ConstraintValidator<SameChannelInOrders, List<SubOrderRequestV1>> {
    override fun isValid(
        orders: List<SubOrderRequestV1>?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (orders.isNullOrEmpty()) return true

        val channels =
            orders
                .map { it.channel }
                .distinct()

        return channels.size <= 1
    }
}
