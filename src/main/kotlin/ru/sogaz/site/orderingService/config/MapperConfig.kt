package ru.sogaz.site.orderingService.config

import org.mapstruct.factory.Mappers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper

@Configuration
class MapperConfig {
    @Bean
    fun orderMapper(): OrderMapper = Mappers.getMapper(OrderMapper::class.java)

    @Bean
    fun paymentEventMapper(): PaymentEventMapper = Mappers.getMapper(PaymentEventMapper::class.java)

    @Bean
    fun messageSource() =
        ResourceBundleMessageSource().apply {
            setBasenames("messages")
            setDefaultEncoding("UTF-8")
            setUseCodeAsDefaultMessage(true)
        }
}
