package ru.sogaz.site.orderingService.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<OrderEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: UUID): Optional<OrderEntity>

    @Query("select o from OrderEntity o where o.orderId = :id")
    fun findByIdWithoutLock(
        @Param("id") id: UUID,
    ): OrderEntity?

    fun findAllByRecipientUserId(userId: String): List<OrderEntity?>

    fun findAllByUnifiedId(unifiedId: String): List<OrderEntity?>

    @Query(
        """
        select o from OrderEntity o
        where (:email is not null and o.recipientEmail = :email)
           or (:phone is not null and o.recipientPhone = :phone)
        """,
    )
    fun findAllByRecipientEmailOrRecipientPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?>

    fun findAllByRecipientEmailAndRecipientPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?>

    @Query(
        """
    select distinct o
      from OrderEntity o
      left join fetch o.subOrders
     where o.status in :states
       and o.paymentEndDate is not null
       and o.paymentEndDate < :now
    """,
    )
    fun findOverdueOrders(
        @Param("states") states: Collection<OrderStatusesEnum>,
        @Param("now") now: Instant,
    ): List<OrderEntity>
}
