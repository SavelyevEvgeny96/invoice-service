package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceStatusEvent(
    val invoiceId: String?,
    val externalSystemCode: String? = null,
    val externalId: String?,
    val status: String?,
    val email: String?,
    val errorText: String? = null,
    val invoices: List<SubInvoiceData>?,
    val rrn: String? = null,
    val qrId: String? = null,
    val maskedPan: String?, // Маскированный номер карты
    val paymentSystem: String?, // Наименование платёжной системы
    val issuerName: String?, // Кем выдана карта (банк-эмитент)
    val title: String?,
    val paymentType: String?, // Источник совершения операции (из portalType)
    val keyCard: String?,
    val bank: String? = null,
    val paySucces: Instant?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubInvoiceData(
    val premium: String?,
    val policyId: String?,
    val policyNumber: String?,
    val policyDate: String?,
    val agreementId: String?,
    val agreementNumber: String?,
    val agreementDate: String?,
    val insuranceKind: String?,
    val channel: String?,
    val program: String?,
)
