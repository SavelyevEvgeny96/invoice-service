package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.repository.OrderRepository
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

open class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val jdbcTemplate: JdbcTemplate,
) : OrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val LOG_ERROR_ORDER_SAVE = "Не удалось сохранить данные по заказу"
    }

    override fun findByIds(ids: List<UUID?>): List<OrderEntity> = orderRepository.findAllById(ids).toList()

    override fun findById(orderId: UUID): OrderEntity? = orderRepository.findById(orderId).getOrNull()

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

    override fun save(order: OrderEntity): OrderEntity = orderRepository.save(order)

    override fun upsertOrdersReturningIds(orders: List<OrderEntity>): List<UUID> {
        if (orders.isEmpty()) return emptyList()

        // 14 параметров: до bank включительно
        // потом хардкодим save_card = TRUE, recurrent = TRUE, status = 'NEW', update_date = NOW()
        val tuple = "(" + List(13) { "?" }.joinToString(", ") + ", TRUE, TRUE, 'NEW', NOW())"
        val valuesSql = orders.joinToString(",") { tuple }

        val sql =
            """
            INSERT INTO orders (
                client_id,
                create_date,
                recipient_email,
                recipient_phone,
                premium_amount,
                payment_end_date,
                key_card,
                unified_id,
                recipient_user_id,
                policyholder,
                payment_type,
                subscription_id,
                bank,
                save_card,
                recurrent,
                status,
                update_date
            )
            VALUES $valuesSql
            RETURNING order_id
            """.trimIndent()

        // 14 параметров на каждую запись
        val args = ArrayList<Any?>(orders.size * 14)
        orders.forEach { o ->
            args += o.clientId
            args += o.createDate?.let { Timestamp.from(it) } // create_date
            args += o.recipientEmail // recipient_email
            args += o.recipientPhone // recipient_phone
            args += o.premiumAmount // premium_amount
            args += o.paymentEndDate?.let { Timestamp.from(it) } // payment_end_date
            args += o.keyCard // key_card
            args += o.unifiedId // unified_id
            args += o.recipientUserId // recipient_user_id
            args += o.policyholder // policyholder
            args += o.paymentType // payment_type
            args += o.subscriptionId // subscription_id
            args += o.bank // bank
            // save_card, recurrent, status, update_date — хардкодом в SQL
        }

        val mapper =
            RowMapper { rs: ResultSet, _: Int ->
                rs.getObject("order_id", UUID::class.java)
            }

        return jdbcTemplate.query(sql, mapper, *args.toTypedArray())
    }
}
