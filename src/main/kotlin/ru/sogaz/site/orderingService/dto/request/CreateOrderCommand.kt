package ru.sogaz.site.orderingService.dto.request

import ru.sogaz.site.orderingService.enums.BankEnum
import java.time.Instant
import java.util.UUID

data class CreateOrderCommand(
    var subOrders: MutableList<CreateSubOrderCommand> = mutableListOf(),
    var orderEndDate: Instant? = null,
    var recipientEmail: String = "",
    var recipientUserId: String? = null,
    var unifiedId: String? = null,
    var recipientPhone: String? = null,
    var urlToReturn: String? = null,
    var urlToDecline: String? = null,
    var saveCard: Boolean = false,
    var subscriptionId: String = "",
    var clientId: String? = null,
    var policyholder: String? = null,
    var bank: BankEnum? = null,
    var orderIdRecurrent: UUID? = null,
    var typePaymentOperation: String? = "",
    var externalId: String? = null,
    var accountCrossId: String = "",
    var apiVersion: String = "",
)
