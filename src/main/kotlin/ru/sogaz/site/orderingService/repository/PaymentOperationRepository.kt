package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import java.util.UUID

@Repository
interface PaymentOperationRepository : JpaRepository<PaymentOperationEntity, UUID> {
    fun findFirstByOrderEntityOrderIdAndState(
        orderId: UUID,
        state: PaymentOperationStateEnum,
    ): PaymentOperationEntity?

    fun findFirstByOrderEntityOrderIdOrderByPayDateDesc(orderId: UUID): PaymentOperationEntity?
}
