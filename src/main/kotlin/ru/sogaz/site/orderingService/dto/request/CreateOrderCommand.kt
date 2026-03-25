package ru.sogaz.site.orderingService.dto.request

import ru.sogaz.site.orderingService.enums.BankEnum
import java.time.Instant
import java.util.UUID

data class CreateOrderCommand(
    val orders: List<CreateSubOrderCommand>,
    val orderEndDate: Instant?,
    val recipientEmail: String?,
    val recipientUserId: String?,
    val unifiedId: String?,
    val recipientPhone: String?,
    val urlToReturn: String?,
    val urlToDecline: String?,
    val saveCard: Boolean,
    val subscriptionId: String?,
    val clientId: String?,
    val policyholder: String?,
    val bank: BankEnum?,
    val orderIdRecurrent: UUID?,
    val typePaymentOperation: String?,
)
