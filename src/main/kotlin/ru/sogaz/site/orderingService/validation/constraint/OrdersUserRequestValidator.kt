package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.enums.OrdersUserConditionEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchNameEnum
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OrdersUserRequestValidator::class])
annotation class ValidateOrdersUserRequest(
    val message: String = "invalid orders user request",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class OrdersUserRequestValidator : ConstraintValidator<ValidateOrdersUserRequest, OrdersUserRequest> {
    override fun isValid(
        req: OrdersUserRequest,
        ctx: ConstraintValidatorContext,
    ): Boolean {
        var valid = true
        ctx.disableDefaultConstraintViolation()

        when (req.searchName) {
            OrdersUserSearchNameEnum.USER_ID -> {
                if (req.userId.isNullOrBlank()) {
                    ctx
                        .buildConstraintViolation("{validation.ordersUserRequest.userId.required}")
                        .addPropertyNode("userId")
                        .addConstraintViolation()
                    valid = false
                }
            }
            OrdersUserSearchNameEnum.GD_ID -> {
                if (req.gdId.isNullOrBlank()) {
                    ctx
                        .buildConstraintViolation("{validation.ordersUserRequest.gdId.required}")
                        .addPropertyNode("gdId")
                        .addConstraintViolation()
                    valid = false
                }
            }
            OrdersUserSearchNameEnum.EMAIL_OR_PHONE -> {
                if (req.condition == null) {
                    ctx
                        .buildConstraintViolation("{validation.ordersUserRequest.condition.required}")
                        .addPropertyNode("condition")
                        .addConstraintViolation()
                    valid = false
                } else {
                    when (req.condition) {
                        OrdersUserConditionEnum.OR -> {
                            if (req.email.isNullOrBlank() && req.phone.isNullOrBlank()) {
                                ctx
                                    .buildConstraintViolation("{validation.ordersUserRequest.emailOrPhone.oneOfRequired}")
                                    .addPropertyNode("email")
                                    .addConstraintViolation()
                                ctx
                                    .buildConstraintViolation("{validation.ordersUserRequest.emailOrPhone.oneOfRequired}")
                                    .addPropertyNode("phone")
                                    .addConstraintViolation()
                                valid = false
                            }
                        }
                        OrdersUserConditionEnum.AND -> {
                            if (req.email.isNullOrBlank() || req.phone.isNullOrBlank()) {
                                if (req.email.isNullOrBlank()) {
                                    ctx
                                        .buildConstraintViolation("{validation.ordersUserRequest.email.required}")
                                        .addPropertyNode("email")
                                        .addConstraintViolation()
                                }
                                if (req.phone.isNullOrBlank()) {
                                    ctx
                                        .buildConstraintViolation("{validation.ordersUserRequest.phone.required}")
                                        .addPropertyNode("phone")
                                        .addConstraintViolation()
                                }
                                valid = false
                            }
                        }
                    }
                }
            }
            null -> {
            }
        }

        return valid
    }

    private fun ConstraintValidatorContext.buildConstraintViolation(
        message: String,
    ): ConstraintValidatorContext.ConstraintViolationBuilder = this.buildConstraintViolationWithTemplate(message)
}
