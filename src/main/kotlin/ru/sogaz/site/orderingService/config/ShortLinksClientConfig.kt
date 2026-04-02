package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.shortlinks.client.api.ShortLinkControllerApi
import ru.sogaz.site.shortlinks.client.invoker.ApiClient

@Configuration
class ShortLinksClientConfig {
    @Bean
    fun shortLinkControllerApi(): ShortLinkControllerApi {
        val apiClient = ApiClient()
        apiClient.basePath
        return ShortLinkControllerApi(apiClient)
    }
}
