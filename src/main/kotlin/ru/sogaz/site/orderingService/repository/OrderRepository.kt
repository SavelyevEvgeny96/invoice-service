package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.OrderEntity

import java.util.*

@Repository
interface OrderRepository : JpaRepository<OrderEntity, UUID> {

    fun findAllByRecipientUserId(userId: String): List<OrderEntity?>

    fun findAllByRecipientUserGdId(gdId: String): List<OrderEntity?>

    @Query(
        """
        select o from OrderEntity o
        where (:email is not null and o.recipientEmail = :email)
           or (:phone is not null and o.recipientPhone = :phone)
        """
    )
    fun findAllByRecipientEmailOrRecipientPhone(email: String?, phone: String?): List<OrderEntity?>

    fun findAllByRecipientEmailAndRecipientPhone(email: String, phone: String): List<OrderEntity?>
}