package ru.sogaz.site.orderingService.apiDoc.integretion

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse

@FeignClient(
    name = "lk-get-gidId",
    url = "\${api.payment.lkCardUrl}",
)
interface LKApi {
    @GetMapping("/api/v1/gid/info/cards")
    fun getGidId(
        @RequestParam gidId: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): GidAuthResponse
}
