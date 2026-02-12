package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServicesConfig {
    @Bean(name = ["RKeyRegexp"])
    fun queueStatusResultNameNormalizeService(): Regex = Regex("[^A-Za-zА-Яа-яЁё0-9]")
}
