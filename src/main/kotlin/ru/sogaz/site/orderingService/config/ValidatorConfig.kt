package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ValidatorConfig {
    @Bean("emailRegex")
    fun emailRegex(): Regex = Regex("^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

    @Bean("phoneRegex")
    fun phoneRegex(): Regex = Regex("^\\+?\\d[\\d ]*$")
}
