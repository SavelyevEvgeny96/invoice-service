package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.CompanyDetailsQrEntity
import java.util.UUID

@Repository
interface CompanyDetailsQrRepository : JpaRepository<CompanyDetailsQrEntity, UUID> {
    fun findByBank(bank: String): CompanyDetailsQrEntity?
}
