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
        var isFullPayerFio = true

        if (value.payerFio?.firstName == null) {
            context.addConstraintForPayerFio("firstName")
            isFullPayerFio = false
        }
        if (value.payerFio?.middleName == null) {
            context.addConstraintForPayerFio("middleName")
            isFullPayerFio = false
        }
        if (value.payerFio?.lastName == null) {
            context.addConstraintForPayerFio("lastName")
            isFullPayerFio = false
        }
        return isFullPayerFio
    }

    private fun ConstraintValidatorContext.addConstraintForPayerFio(field: String) {
        disableDefaultConstraintViolation()
        buildConstraintViolationWithTemplate("{validation.orderRequest.payerFio.required}")
            .addPropertyNode(field)
            .addConstraintViolation()
    }
}
