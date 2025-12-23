package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import java.util.*

interface ClientSystemRepository : JpaRepository<ClientSystemEntity, UUID> {
    fun findAllByExternalSystemCodeInAndPermissionReturnTrue(codes: Collection<String>): List<ClientSystemEntity>
}