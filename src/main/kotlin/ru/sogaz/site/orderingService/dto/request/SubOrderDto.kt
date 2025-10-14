package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SubOrderDto(
    @field:JsonProperty("premiumAmount")
    @field:NotNull
    @field:DecimalMin("0.01")
    val premiumAmountDto: BigDecimal, // сумма премии (обязательно)
    @field:NotNull
    val policyId: String, // идентификатор полиса
    @field:NotNull
    val operationId: String,
    @field:NotNull
    val policyNumber: String, // номер полиса
    val contractNumber: String?, // идентификатор договора
    val contractId: String?, // номер договора
    val typeInsurance: String?, // вид страхования (ОСАГО, КАСКО и т.д.)
    val insuranceProgram: String?, // программа страхования
)
