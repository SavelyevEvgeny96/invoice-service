package ru.sogaz.site.orderingService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.dao.CompanyDetailsQrDao
import ru.sogaz.site.orderingService.entity.CompanyDetailsQrEntity
import ru.sogaz.site.orderingService.repository.CompanyDetailsQrRepository

@Repository
class CompanyDetailsQrDaoImpl(
    private val companyDetailsQrRepository: CompanyDetailsQrRepository,
) : CompanyDetailsQrDao {
    override fun findByBank(bank: String): CompanyDetailsQrEntity? = companyDetailsQrRepository.findByBank(bank)
}
