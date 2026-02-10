package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import java.util.UUID

@Repository
interface PaymentOperationRepository : JpaRepository<PaymentOperationEntity, UUID>
