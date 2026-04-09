package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO для запроса на создание подзаказа v2.
 */
data class SubOrderRequestV2(
    @field:NotNull(message = "{validation.orderRequest.premiumAmount.notNull}")
    @field:Positive(message = "{validation.orderRequest.premiumAmount.positive}")
    val premium: BigDecimal = BigDecimal.ZERO,
    val policyId: String = "",
    val policyNumber: String = "",
    val policyDate: Instant? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val agreementNumber: String? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val agreementId: String? = null,
    val agreementDate: Instant? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    val insuranceKind: String? = null,
    val program: String? = null,
    val channel: String? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    var typeOperation: String = "",
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    val managerEmail: String = "",
)
