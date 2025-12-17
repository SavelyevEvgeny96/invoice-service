package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.ClientSystemEntity

interface ClientSystemDao {
    fun getClientSystem(
        traceId: String,
        externalSystemCode: String,
    ): ClientSystemEntity

    fun findBySystemCode(externalSystemCode: String?): ClientSystemEntity?
}
