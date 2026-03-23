package ru.sogaz.site.orderingService.dto.request

import java.math.BigDecimal
import java.time.Instant

data class CreateSubOrderCommand(
    val premiumAmount: BigDecimal,
    val policyId: String = "",
    val policyNumber: String = "",
    val policyDate: Instant? = null,
    val contractNumber: String? = null,
    val contractId: String? = null,
    val contractDate: Instant? = null,
    val typeInsurance: String? = null,
    val insuranceProgram: String? = null,
    val docType: String? = null,
    val sendStatusProduct: Boolean? = false,
    val managerEmail: String = "",
    val channel: String = "",
    val mainContractCheck: Boolean = false,
)
