package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.validation.constraint.Phone
import ru.sogaz.site.orderingService.validation.constraint.SogazDomain
import java.time.Instant

/**
 * DTO для запроса на создание заказа v2.
 */
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
    var bank: BankEnum? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    var typePaymentOperation: String = "",
    var clientId: String? = null,
)
