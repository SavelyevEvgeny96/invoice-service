package ru.sogaz.site.orderingService.dto.request

import java.math.BigDecimal
import java.time.Instant

data class CreateSubOrderCommand(
    val premiumAmount: BigDecimal,
    val policyId: String,
    val policyNumber: String,
    val policyDate: Instant?,
    val contractNumber: String?,
    val contractId: String?,
    val contractDate: Instant?,
    val typeInsurance: String?,
    val insuranceProgram: String?,
    val docType: String?,
    val sendStatusProduct: Boolean,
    val managerEmail: String,
    val channel: String,
    val mainContractCheck: Boolean,
)
