package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.validation.constraint.EmailValidator
import ru.sogaz.site.orderingService.validation.constraint.PhoneValidator


@Configuration
open class ValidatorConfig {
    companion object {
        const val EMAIL_REGEX_STRING = "^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        // Разрешены цифры, пробелы и один '+' только в начале
        const val PHONE_REGEX_STRING = "^\\+?\\d[\\d ]*$"
    }

    @Bean
    open fun emailValidator(): EmailValidator =
        Regex(EMAIL_REGEX_STRING).run(::EmailValidator)

    @Bean
    open fun phoneValidator(): PhoneValidator =
        Regex(PHONE_REGEX_STRING).run(::PhoneValidator)

}