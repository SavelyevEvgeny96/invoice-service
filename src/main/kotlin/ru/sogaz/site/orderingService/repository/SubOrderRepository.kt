package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.util.UUID

@Repository
interface SubOrderRepository : JpaRepository<SubOrderEntity, UUID> {
    fun findAllByOrderEntityOrderId(orderId: UUID?): List<SubOrderEntity?>

    fun findByOrderEntityOrderIdAndMainContractCheckTrue(orderId: UUID): SubOrderEntity?
}
