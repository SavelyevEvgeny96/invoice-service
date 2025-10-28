package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import java.sql.Timestamp
import java.util.UUID

class SubOrderDaoImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val subOrderRepository: SubOrderRepository,
) : SubOrderDao {
    companion object {
        private const val LOG_START = "Старт batch upsertSubOrders: size=%d"
        private const val LOG_EXECUTE = "Выполняем batchUpdate() для %d записей"
        private const val LOG_DONE = "Завершён upsertSubOrders: size=%d"
        private const val GET_SUB_ORDER_LIST = "Получение списка sub_orders по orderId: %s"
    }

    private val logger = loggerFor(javaClass)

    override fun upsertSubOrders(subs: List<SubOrderEntity>) {
        val sql =
            """
            INSERT INTO sub_orders (
                order_id, policy_id, policy_number, contract_id, contract_number,
                insurance_program, type_insurance, premium_amount, manager_email,
                doc_type,channel,main_contract_check,contract_date,policy_date
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (policy_id) DO UPDATE
              SET policy_number     = EXCLUDED.policy_number,
                  contract_id       = EXCLUDED.contract_id,
                  policy_id         = EXCLUDED.policy_id,
                  contract_number   = EXCLUDED.contract_number,
                  insurance_program = EXCLUDED.insurance_program,
                  type_insurance    = EXCLUDED.type_insurance,
                  premium_amount    = EXCLUDED.premium_amount,
                  manager_email     = EXCLUDED.manager_email,
                  order_id          = EXCLUDED.order_id,
                  doc_type          = EXCLUDED.doc_type,
                  channel           = EXCLUDED.channel,
                  main_contract_check = EXCLUDED.main_contract_check,
                  contract_date     = EXCLUDED.contract_date,
                  policy_date       = EXCLUDED.policy_date
            """.trimIndent()

        logger.info(LOG_START.format(subs.size))

        jdbcTemplate.batchUpdate(sql, subs, subs.size) { ps, s ->
            ps.setObject(1, s.orderEntity?.orderId)
            ps.setString(2, s.policyId)
            ps.setString(3, s.policyNumber)
            ps.setString(4, s.contractId)
            ps.setString(5, s.contractNumber)
            ps.setString(6, s.insuranceProgram)
            ps.setString(7, s.typeInsurance)
            ps.setBigDecimal(8, s.premiumAmount)
            ps.setString(9, s.managerEmail)
            ps.setString(10, s.docType)
            ps.setString(11, s.channel)
            ps.setObject(12, s.mainContractCheck)
            ps.setTimestamp(13, s.contractDate?.let { Timestamp.from(it) })
            ps.setTimestamp(13, s.policyDate?.let { Timestamp.from(it) })
        }

        logger.info(LOG_EXECUTE.format(subs.size))
        logger.info(LOG_DONE.format(subs.size))
    }

    override fun findByOrderId(orderId: UUID?): List<SubOrderEntity?> {
        logger.info(GET_SUB_ORDER_LIST.format(orderId))
        return subOrderRepository.findAllByOrderEntityOrderId(orderId)
    }
}
