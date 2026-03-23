package ru.sogaz.site.orderingService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.payment.client.api.PayV2Api
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi

@Configuration
class ApiClientConfig {
    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun paymentApiClient(
        @Value("\${app.client.payment.basePath}") paymentBasePath: String,
    ) = ru.sogaz.site.payment.client.invoker.ApiClient().apply {
        basePath = paymentBasePath
    }

    @Bean
    fun payClient(paymentApiClient: ru.sogaz.site.payment.client.invoker.ApiClient): PayV2Api = PayV2Api(paymentApiClient)

    @Bean
    fun qrApiClient(
        @Value("\${app.client.qr.basePath}") qrGeneratorBasePath: String,
    ) = ru.sogaz.site.qr.generator.client.invoker.ApiClient().apply {
        basePath = qrGeneratorBasePath
    }

    @Bean
    fun qrCodeControllerApi(qrApiClient: ru.sogaz.site.qr.generator.client.invoker.ApiClient): QrCodeControllerApi =
        QrCodeControllerApi(qrApiClient)
}
