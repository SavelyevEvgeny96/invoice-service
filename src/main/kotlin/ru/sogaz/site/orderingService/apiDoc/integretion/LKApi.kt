package ru.sogaz.site.orderingService.apiDoc.integretion
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.orderingService.dto.response.GidAuthResponse

@FeignClient(
    name = "lk-get-gidId",
    url = "\${api.payment.lkBasePath}"
)
interface LKApi {
    @PostMapping(value = ["/api/v1/gid/info/cards"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getGidId(
        @RequestParam gidId: String?,
    ): GidAuthResponse
}
