package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.validation.constraint.Phone
import ru.sogaz.site.orderingService.validation.constraint.RussianNameValid
import ru.sogaz.site.orderingService.validation.constraint.SameChannelInOrders
import java.time.Instant

/**
 * DTO для запроса на создание заказа v2.
 */
data class OrderRequestV2(
    @get:Valid
    @field:SameChannelInOrders(message = "{validation.orderRequest.sameChannelInOrders}")
    var orders: MutableList<SubOrderRequestV2> = mutableListOf(),
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    var recipientEmail: String = "",
    @field:Phone(message = "{validation.ordersUserRequest.phone.invalid}")
    var recipientPhone: String? = null,
    var recipientUserId: String? = null,
    var unifiedId: String? = null,
    @field:RussianNameValid
    var policyholder: String? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    var saveCard: Boolean = false,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    @field:Future(message = "{validation.orderRequest.date.future}")
    var orderEndDate: Instant? = null,
    var urlToReturn: String? = null,
    var urlToDecline: String? = null,
    var subscriptionId: String = "",
    var bank: BankEnum? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    var typePaymentOperation: String = "",
    var clientId: String? = null,
)
