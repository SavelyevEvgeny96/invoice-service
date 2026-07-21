package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.enums.PaymentMethod
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OrderRequestV2Validator::class])
annotation class ValidOrderRequestV2(
    val message: String = "Order request v2 is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class OrderRequestV2Validator : ConstraintValidator<ValidOrderRequestV2, OrderRequestV2> {
    override fun isValid(
        value: OrderRequestV2?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) return true

        val paymentMethods = value.paymentMethodList
        var valid = true

        if (paymentMethods != null && paymentMethods.isEmpty()) {
            context.addViolation("paymentMethodList", "{validation.orderRequest.paymentMethodList.notEmpty}")
            valid = false
        }

        if (paymentMethods.orEmpty().contains(PaymentMethod.QR_BANKING_DETAILS) && value.payerFio?.isFilled() != true) {
            context.addViolation("payerFio", "{validation.orderRequest.payerFio.required}")
            valid = false
        }

        return valid
    }

    private fun ConstraintValidatorContext.addViolation(
        property: String,
        message: String,
    ) {
        disableDefaultConstraintViolation()
        buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation()
    }
}
