package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.validation.constraint.EmailValid
import java.math.BigDecimal
import java.time.Instant

data class SubOrderDto(
    @field:JsonProperty("premiumAmount")
    @field:NotNull
    @field:DecimalMin("0.01")
    val premiumAmountDto: BigDecimal, // сумма премии (обязательно)
    @field:NotNull
    val policyId: String, // идентификатор полиса
    @field:NotNull
    val policyNumber: String, // номер полиса
    val contractNumber: String?, // идентификатор договора
    val contractId: String?, // номер договора
    val typeInsurance: String?, // вид страхования (ОСАГО, КАСКО и т.д.)
    val insuranceProgram: String?, // программа страхования
    val mainContractCheck: Boolean = false,
    val policyDate: Instant? = null,
    @field:NotNull
    @field:FutureOrPresent
    val contractDate: Instant? = null,
    @field:EmailValid
    val managerEmail: String = "",
    @field:NotBlank
    val channel: String = "",
    val docType: String? = null,
)
