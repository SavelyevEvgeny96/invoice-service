package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class PayerFio
    @JsonCreator
    constructor(
        @param:JsonProperty("lastName")
        @get:JsonProperty("lastName")
        @field:NotBlank(message = "{validation.orderRequest.payerFio.lastName.required}")
        val lastName: String? = null,
        @param:JsonProperty("firstName")
        @get:JsonProperty("firstName")
        @field:NotBlank(message = "{validation.orderRequest.payerFio.firstName.required}")
        val firstName: String? = null,
        @param:JsonProperty("middleName")
        @get:JsonProperty("middleName")
        val middleName: String? = null,
    )
