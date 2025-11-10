package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.repository.OrderRepository
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

open class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val jdbcTemplate: JdbcTemplate,
) : OrderDao {
    override fun findByRecipientUserId(userId: String): List<OrderEntity?> = orderRepository.findAllByRecipientUserId(userId)

    override fun findByUnifiedId(unifiedId: String): List<OrderEntity?> = orderRepository.findAllByUnifiedId(unifiedId)

    override fun findByEmailOrPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailOrRecipientPhone(email, phone)

    override fun findByEmailAndPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailAndRecipientPhone(email, phone)

    override fun upsertOrdersReturningIds(orders: List<OrderEntity>): List<UUID> {
        if (orders.isEmpty()) return emptyList<UUID>()

        // 16 параметров, status и update_date — константы
        val tuple = "(" + List(16) { "?" }.joinToString(", ") + ", 'NEW', NOW())"
        val valuesSql = orders.joinToString(",") { tuple }

        val sql =
            """
            INSERT INTO orders (
                create_date,
                recipient_email,
                recipient_phone,
                premium_amount,
                payment_end_date,
                key_card,
                save_card,
                recurrent,
                unified_id,
                recipient_user_id,
                url_to_return,
                url_to_decline,
                policyholder,
                payment_type,
                subscription_id,
                bank,
                status,
                update_date
            )
            VALUES $valuesSql
            RETURNING order_id
            """.trimIndent()

        val args = ArrayList<Any?>(orders.size * 16)
        orders.forEach { o ->
            args += o.createDate?.let { Timestamp.from(it) }
            args += o.recipientEmail
            args += o.recipientPhone
            args += o.premiumAmount
            args += o.paymentEndDate?.let { Timestamp.from(it) }
            args += o.keyCard
            args += o.saveCard
            args += o.recurrent
            args += o.unifiedId
            args += o.recipientUserId
            args += o.urlToReturn
            args += o.urlToDecline
            args += o.policyholder
            args += o.paymentType
            args += o.subscriptionId
            args += o.bank
        }

        val mapper =
            RowMapper { rs: ResultSet, _: Int ->
                rs.getObject("order_id", UUID::class.java)
            }

        return jdbcTemplate.query(sql, mapper, *args.toTypedArray())
    }
}
