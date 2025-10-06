package ru.sogaz.site.orderingService.dto.data

import java.util.UUID

data class PublishResult(
    val acked: Set<UUID>,
    val nAcked: Map<UUID, String?>,
    val timeouts: Set<UUID>,
)
