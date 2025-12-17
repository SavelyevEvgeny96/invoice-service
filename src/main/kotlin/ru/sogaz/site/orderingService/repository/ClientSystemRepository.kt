package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.ClientSystemEntity

@Repository
interface ClientSystemRepository : JpaRepository<ClientSystemEntity, Long> {
    fun findByExternalSystemCode(externalSystemCode: String?): ClientSystemEntity?
}
