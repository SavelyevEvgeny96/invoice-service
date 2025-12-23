package ru.sogaz.site.orderingService.dao

interface ClientSystemDao {
    fun checkingRefundAccess(code: String): Boolean
}