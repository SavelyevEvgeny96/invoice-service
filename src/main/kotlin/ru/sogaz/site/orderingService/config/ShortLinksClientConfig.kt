package ru.sogaz.site.orderingService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.shortlinks.client.api.ShortLinkControllerApi
import ru.sogaz.site.shortlinks.client.invoker.ApiClient

@Configuration
class ShortLinksClientConfig {

    @Bean
    fun shortLinkControllerApi(
        @Value("\${app.client.shortLink.basePath}") shortLinkBasePath: String
    ): ShortLinkControllerApi {
        val apiClient = ApiClient().apply {
            basePath = shortLinkBasePath
        }
        return ShortLinkControllerApi(apiClient)
    }
}