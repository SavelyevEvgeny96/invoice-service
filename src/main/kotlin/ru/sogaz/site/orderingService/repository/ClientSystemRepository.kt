package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import java.util.UUID


@Repository
interface ClientSystemRepository : JpaRepository<ClientSystemEntity, UUID> {
    fun findByExternalSystemCode(externalSystemCode: String?): ClientSystemEntity?
    fun findAllByExternalSystemCodeInAndPermissionReturnTrue(codes: Collection<String>): List<ClientSystemEntity>
}
