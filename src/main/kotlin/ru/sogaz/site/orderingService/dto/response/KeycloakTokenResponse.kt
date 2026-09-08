package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KeycloakTokenResponse(
    @field:JsonProperty("access_token")
    val accessToken: String?,
)
