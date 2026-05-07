package ru.sogaz.site.orderingService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rabbit.listener")
data class RabbitListenerProps(
    val consumers: Int,
    val maxConsumers: Int,
    var concurrency: Int?,
    var maxConcurrency: Int?,
    val stopConsumerMinIntervalMs: Long,
)
