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

    override fun findByUnifiedId(unifiedId: String): List<OrderEntity?> = orderRepository.findAllByUnifiedId(unifiedId)

    override fun findByEmailOrPhone(
        email: String?,
        phone: String?,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailOrRecipientPhone(email, phone)

    override fun findByEmailAndPhone(
        email: String,
        phone: String,
    ): List<OrderEntity?> = orderRepository.findAllByRecipientEmailAndRecipientPhone(email, phone)

    override fun upsertOrders(orders: List<OrderEntity>) {
        val sqlSetImmediate = "SET CONSTRAINTS ALL IMMEDIATE"
        val sqlInsert = """
        INSERT INTO orders (
            create_date, recipient_email, recipient_phone,
            premium_amount, payment_end_date, key_card, save_card,
            recurrent, unified_id, recipient_user_id,
            url_to_return, url_to_decline, policyholder,
            payment_type, subscription_id, status, update_date
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', NOW())
        ON CONFLICT (subscription_id) DO UPDATE
          SET recipient_email   = EXCLUDED.recipient_email,
              recipient_phone   = EXCLUDED.recipient_phone,
              premium_amount    = EXCLUDED.premium_amount,
              payment_end_date  = EXCLUDED.payment_end_date,
              key_card          = EXCLUDED.key_card,
              save_card         = EXCLUDED.save_card,
              recurrent         = EXCLUDED.recurrent,
              unified_id        = EXCLUDED.unified_id,
              recipient_user_id = EXCLUDED.recipient_user_id,
              url_to_return     = EXCLUDED.url_to_return,
              url_to_decline    = EXCLUDED.url_to_decline,
              policyholder      = EXCLUDED.policyholder,
              payment_type      = EXCLUDED.payment_type,
              update_date       = NOW()
    """.trimIndent()
        val sqlSetDeferred = "SET CONSTRAINTS ALL DEFERRED"

        logger.info(LOG_START.format(orders.size))

        jdbcTemplate.execute(sqlSetImmediate)
        jdbcTemplate.batchUpdate(sqlInsert, orders, orders.size) { ps, o ->
            ps.setTimestamp(1, o.createDate?.let { Timestamp.from(it) })
            ps.setString(2, o.recipientEmail)
            ps.setString(3, o.recipientPhone)
            ps.setBigDecimal(4, o.premiumAmount)
            ps.setTimestamp(5, o.paymentEndDate?.let { Timestamp.from(it) })
            ps.setString(6, o.keyCard)
            ps.setObject(7, o.saveCard)
            ps.setObject(8, o.recurrent)
            ps.setString(9, o.unifiedId)
            ps.setString(10, o.recipientUserId)
            ps.setString(11, o.urlToReturn)
            ps.setString(12, o.urlToDecline)
            ps.setString(13, o.policyholder)
            ps.setString(14, o.paymentType)
            ps.setString(15, o.subscriptionId)
        }
        jdbcTemplate.execute(sqlSetDeferred)

        logger.info(LOG_EXECUTE.format(orders.size))
        logger.info(LOG_DONE.format(orders.size))
    }
}
