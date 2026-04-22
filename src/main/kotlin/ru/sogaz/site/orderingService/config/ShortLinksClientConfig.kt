package ru.sogaz.site.orderingService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ShortLinksClientConfig {
    @Bean
    fun shortLinkControllerApi(@Value("\${app.client.shortLink.basePath}") shortLinkBasePath: String) =
        ru.sogaz.site.shortlinks.client.invoker.ApiClient().apply { basePath = shortLinkBasePath }
}
