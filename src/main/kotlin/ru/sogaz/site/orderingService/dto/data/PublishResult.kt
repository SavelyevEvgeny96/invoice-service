package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

data class PublishResult(
    val acked: MutableSet<UUID?>,
    val nAcked: MutableMap<UUID?, String?>,
    val timeouts: MutableSet<UUID?>,
)
