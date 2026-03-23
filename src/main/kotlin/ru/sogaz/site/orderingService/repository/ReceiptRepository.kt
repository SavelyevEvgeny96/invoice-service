package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.orderingService.entity.ReceiptEntity
import java.util.UUID

interface ReceiptRepository : JpaRepository<ReceiptEntity, UUID>
