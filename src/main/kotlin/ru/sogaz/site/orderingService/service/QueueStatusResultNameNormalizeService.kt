package ru.sogaz.site.orderingService.service

interface QueueStatusResultNameNormalizeService {
    fun buildQueueStatusResultName(
        pattern: String,
        clientId: String,
    ): String
}
