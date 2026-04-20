package ru.sogaz.site.orderingService.dto.data

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.orderingService.enums.OperationTypeEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CompletedPaymentData(
    @field:JsonProperty
    val paymentId: UUID,
    val orderId: UUID,
    val totalAmount: BigDecimal,
    val depersonalization: Boolean = false,
    val status: PaymentOperationStateEnum,
    val card: ClientCardDetails? = null,
    val bank: String,
    val paymentBankId: String?,
    val payerIp: String?,
    val paymentType: String,
    val operationType: OperationTypeEnum,
    val payDate: Instant,
    val externalErrorCode: String?,
    val errorText: String?,
)

data class ClientCardDetails(
    val maskedPan: String?, // Маскированный номер карты
    val paymentSystem: String?, // Наименование платёжной системы
    val issuerName: String?, // Кем выдана карта (банк-эмитент)
    val paymentType: String?, // Источник совершения операции (из portalType)
    val cardId: String?,
    val title: String?,
) {
    constructor(keyCard: String) : this(null, null, null, null, keyCard, null)
}
