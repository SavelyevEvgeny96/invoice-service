package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.site.orderingService.enums.PaymentMethod

class PayerFioForQrValidator : ConstraintValidator<ValidPayerFioForQr, OrderRequestV2> {
    override fun isValid(
        value: OrderRequestV2?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null || PaymentMethod.QR_BANKING_DETAILS !in value.paymentMethodList.orEmpty()) return true
        if (value.payerFio != null) return true

        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate("{validation.orderRequest.payerFio.required}")
            .addPropertyNode("payerFio")
            .addConstraintViolation()
        return false
    }
}
