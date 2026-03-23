package ru.sogaz.site.orderingService.dto.request

import ru.sogaz.site.orderingService.enums.BankEnum
import java.time.Instant
import java.util.UUID

data class CreateOrderCommand(
    val orders: List<CreateSubOrderCommand>,
    val orderEndDate: Instant?,
    val recipientEmail: String,
    val recipientUserId: String? = null,
    val unifiedId: String? = null,
    val recipientPhone: String? = null,
    val urlToReturn: String? = null,
    val urlToDecline: String? = null,
    val saveCard: Boolean = false,
    val subscriptionId: String = "",
    val clientId: String? = null,
    val policyholder: String? = null,
    val bank: BankEnum? = null,
    var orderIdRecurrent: UUID? = null,
    val typePaymentOperation: String? = null,
)
