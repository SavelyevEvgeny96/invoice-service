package ru.sogaz.site.orderingService.dto.data

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class MetaInfoOrder(
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss+00:00",
        timezone = "UTC",
    )
    val eventTimeIso: Instant?,
    val author: String,
    val routingKey: String,
)
