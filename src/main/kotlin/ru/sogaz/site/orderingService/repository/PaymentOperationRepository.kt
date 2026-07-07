package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
    @Query(
        value = """
        select *
        from payment_operations
        where order_id = :orderId
        order by pay_date desc
        limit 1
    """,
        nativeQuery = true
    )
    fun findOldestByOrderId(
        @Param("orderId") orderId: UUID
    ): PaymentOperationEntity?
    fun findFirstByOrderEntityOrderIdOrderByPayDateAsc(orderId: UUID): PaymentOperationEntity?
}
