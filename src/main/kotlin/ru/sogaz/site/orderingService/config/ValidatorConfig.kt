package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.jwt.starter.service.JwtService
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.service.impl.AuthorizationServiceImpl

@Configuration
open class ValidatorConfig {
    @Bean("emailRegex")
    fun emailRegex(): Regex = Regex("^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

    @Bean("phoneRegex")
    fun phoneRegex(): Regex = Regex("^\\+?\\d[\\d ]*$")

    @Bean
    open fun tokenValidator(
        clientSystemDao: ClientSystemDao,
        jwtService: JwtService,
    ) = AuthorizationServiceImpl(
        clientSystemDao = clientSystemDao,
        jwtService = jwtService,
    )
}
