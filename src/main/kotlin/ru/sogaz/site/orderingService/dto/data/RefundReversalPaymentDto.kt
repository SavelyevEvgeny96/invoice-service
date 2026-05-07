package ru.sogaz.site.orderingService.dto.data

import ru.sogaz.site.orderingService.enums.BankEnum
import java.math.BigDecimal

/**
 * Сообщение для запуска операции отмены платежа в payment-service.
 */
data class RefundReversalPaymentDto(
    val paymentBankId: String,
    val amount: BigDecimal,
    val bank: BankEnum,
    val type: String,
    val description: String,
)
