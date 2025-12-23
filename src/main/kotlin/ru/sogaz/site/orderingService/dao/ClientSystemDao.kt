package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.ClientSystemEntity

interface ClientSystemDao {
    fun checkingRefundAccess(codes: Collection<String>): List<ClientSystemEntity>
}