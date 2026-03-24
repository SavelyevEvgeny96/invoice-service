package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO для запроса на создание заказа.
 */
data class SubOrderRequestV1(
    @field:NotNull(message = "{validation.orderRequest.premiumAmount.notNull}")
    @field:Positive(message = "{validation.orderRequest.premiumAmount.positive}")
    val premiumAmount: BigDecimal = BigDecimal.ZERO,
    val policyId: String = "",
    val policyNumber: String = "",
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    val typeInsurance: String? = null,
    val insuranceProgram: String? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    override val mainContractCheck: Boolean = false,
    val sendStatusProduct: Boolean? = false,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val contractNumber: String? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val contractId: String? = null,
    val docType: String? = null,
    val policyDate: Instant? = null,
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    val contractDate: Instant? = null,
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    val managerEmail: String = "",
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val channel: String = "",
) : HasMainContractCheck
