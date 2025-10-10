package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.repository.OrderRepository
import java.sql.Timestamp

class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val jdbcTemplate: JdbcTemplate,
) : OrderDao {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val LOG_EXECUTE = "Выполняем batchUpdate() для %d записей"
        private const val LOG_DONE = "Завершён upsertOrders: size=%d"
    }

    private val logger = loggerFor(javaClass)

    override fun findByRecipientUserId(userId: String): List<OrderEntity?> = orderRepository.findAllByRecipientUserId(userId)

    override fun findByRecipientGdId(gdId: String): List<OrderEntity?> = orderRepository.findAllByRecipientUserGdId(gdId)

    override fun findByEmailOrPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailOrRecipientPhone(email, phone)

    override fun findByEmailAndPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailAndRecipientPhone(email, phone)

    override fun upsertOrders(orders: List<OrderEntity>) {
        val sql =
            """
            INSERT INTO orders (
                order_id, create_date, recipient_email, recipient_phone,
                premium_amount, payment_end_date, key_card, save_card,
                recurrent, recipient_user_gd_id, recipient_user_id, status, update_date
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', NOW())
            ON CONFLICT (order_id) DO UPDATE
              SET recipient_email      = EXCLUDED.recipient_email,
                  recipient_phone      = EXCLUDED.recipient_phone,
                  premium_amount       = EXCLUDED.premium_amount,
                  payment_end_date     = EXCLUDED.payment_end_date,
                  key_card             = EXCLUDED.key_card,
                  save_card            = EXCLUDED.save_card,
                  recurrent            = EXCLUDED.recurrent,
                  recipient_user_gd_id = EXCLUDED.recipient_user_gd_id,
                  recipient_user_id    = EXCLUDED.recipient_user_id,
                  -- если текущий статус НЕ терминальный — переведём в UPDATE, иначе оставим как есть
                  status               = CASE
                                            WHEN orders.status IN ('SUCCESS', 'OVERDUE', 'MARKEDDEL') THEN orders.status
                                            ELSE 'UPDATE'
                                         END,
                  update_date          = NOW()
            """.trimIndent()

        logger.info(LOG_START.format(orders.size))

        jdbcTemplate.batchUpdate(sql, orders, orders.size) { ps, o ->
            ps.setObject(1, o.orderId)
            ps.setTimestamp(2, o.createDate?.let { Timestamp.from(it) })
            ps.setString(3, o.recipientEmail)
            ps.setString(4, o.recipientPhone)
            ps.setBigDecimal(5, o.premiumAmount)
            ps.setTimestamp(6, o.paymentEndDate?.let { Timestamp.from(it) })
            ps.setString(7, o.keyCard)
            ps.setObject(8, o.saveCard)
            ps.setObject(9, o.recurrent)
            ps.setString(10, o.recipientUserGdId)
            ps.setString(11, o.recipientUserId)
        }

        logger.info(LOG_EXECUTE.format(orders.size))
        logger.info(LOG_DONE.format(orders.size))
    }
}
