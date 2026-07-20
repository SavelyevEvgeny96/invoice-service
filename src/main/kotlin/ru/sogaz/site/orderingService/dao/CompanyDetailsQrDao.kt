package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.CompanyDetailsQrEntity

interface CompanyDetailsQrDao {
    fun findByBank(bank: String): CompanyDetailsQrEntity?
}
