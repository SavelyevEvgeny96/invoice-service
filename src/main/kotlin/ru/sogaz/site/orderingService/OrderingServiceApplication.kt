package ru.sogaz.site.orderingService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.slf4j.LoggerFactory

@SpringBootApplication
@ConfigurationPropertiesScan("ru.sogaz.site.orderingService.properties")
class OrderingServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderingServiceApplication>(*args)
}

fun <T> loggerFor(clazz: Class<T>) = LoggerFactory .getLogger(clazz)
