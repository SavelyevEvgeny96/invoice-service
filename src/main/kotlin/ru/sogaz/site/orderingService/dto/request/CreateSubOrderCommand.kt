package ru.sogaz.site.orderingService.dto.request

import java.math.BigDecimal
import java.time.Instant

data class CreateSubOrderCommand(
    var premiumAmount: BigDecimal,
    var policyId: String = "",
    var policyNumber: String = "",
    var policyDate: Instant? = null,
    var contractNumber: String? = null,
    var contractId: String? = null,
    var contractDate: Instant? = null,
    var typeInsurance: String? = null,
    var insuranceProgram: String? = null,
    var docType: String? = null,
    var sendStatusProduct: Boolean? = false,
    var managerEmail: String = "",
    var channel: String = "",
    var mainContractCheck: Boolean = false,
    val typeOperation: String? = null,
)
