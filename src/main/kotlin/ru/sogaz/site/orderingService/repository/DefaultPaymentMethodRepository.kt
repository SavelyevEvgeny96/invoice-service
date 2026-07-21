package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.orderingService.entity.DefaultPaymentMethodEntity
import java.util.UUID

interface DefaultPaymentMethodRepository : JpaRepository<DefaultPaymentMethodEntity, UUID> {
    fun findAllByAvailabilityTrue(): List<DefaultPaymentMethodEntity>
}
