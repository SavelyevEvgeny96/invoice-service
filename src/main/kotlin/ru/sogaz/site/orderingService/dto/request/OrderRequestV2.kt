package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.enums.PaymentQrBank
import ru.sogaz.site.orderingService.validation.constraint.Phone
import ru.sogaz.site.orderingService.validation.constraint.SogazDomain
import ru.sogaz.site.orderingService.validation.constraint.ValidPayerFioForQr
import java.time.Instant

/**
 * DTO для запроса на создание заказа v2.
 */
@ValidPayerFioForQr
data class OrderRequestV2(
    @field:Valid
    var invoices: MutableList<SubOrderRequestV2> = mutableListOf(),
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    var email: String = "",
    @field:Phone(message = "{validation.ordersUserRequest.phone.invalid}")
    var phoneNumber: String? = null,
    var recipientUserId: String? = null,
    var unifiedId: String? = null,
    var externalId: String? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    var saveCard: Boolean? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    @field:Future(message = "{validation.orderRequest.date.future}")
    var invoiceEndDate: Instant? = null,
    @param:Parameter(name = "urlToReturnS", description = "URL для перехода после успешной оплаты")
    @field:SogazDomain
    var urlToReturn: String? = null,
    @param:Parameter(name = "urlToDecline", description = "URL для перехода после неуспешной оплаты ")
    @field:SogazDomain
    var urlToDecline: String? = null,
    var accountCrossId: String = "",
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    var typePaymentOperation: String = "",
    var clientId: String? = null,
    var paymentMethodList: List<PaymentMethod>? = null,
    @param:JsonAlias("bankQR")
    @param:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    var bankQr: List<PaymentQrBank>? = null,
    @param:JsonProperty("payerFio")
    @get:JsonProperty("payerFio")
    @field:Valid
    var payerFio: PayerFio? = null,
    var checkUrlReturn: Boolean? = null,
    var checkPaymentInformation: Boolean? = null
)

data class PayerFio(
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
) {
    fun isFilled(): Boolean =
        !lastName.isNullOrBlank() &&
            !firstName.isNullOrBlank() &&
            !middleName.isNullOrBlank()
}
