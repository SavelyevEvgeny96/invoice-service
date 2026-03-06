package ru.sogaz.site.orderingService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.payment.client.api.PayV2Api
import ru.sogaz.site.payment.client.invoker.ApiClient

@Configuration
class ApiClientConfig {
    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun paymentApiClient(
        @Value("\${app.payment.client.basePath}") paymentBasePath: String,
    ) = ApiClient().apply {
        basePath = paymentBasePath
    }

    @Bean
    fun payClient(paymentApiClient: ApiClient): PayV2Api = PayV2Api(paymentApiClient)
}
