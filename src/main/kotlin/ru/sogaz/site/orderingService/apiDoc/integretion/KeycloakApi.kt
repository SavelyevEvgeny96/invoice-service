package ru.sogaz.site.orderingService.apiDoc.integretion

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.util.MultiValueMap
import ru.sogaz.site.orderingService.dto.response.KeycloakTokenResponse

@FeignClient(
    name = "lk-keycloak-token",
    url = "\${api.payment.keycloakUrl}",
)
interface KeycloakApi {
    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun getToken(@RequestBody request: MultiValueMap<String, String>): KeycloakTokenResponse
}
