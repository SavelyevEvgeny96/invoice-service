package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import java.sql.Timestamp
import java.util.UUID

@Service
open class SubOrderDaoImpl(
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
        if (subs.isEmpty()) return

        // 14 плейсхолдеров под все колонки
        val tuple = "(" + List(14) { "?" }.joinToString(", ") + ")"
        val valuesSql = subs.joinToString(",") { tuple }

        val sql =
            """
            INSERT INTO sub_orders (
                order_id, policy_id, policy_number, contract_id, contract_number,
                insurance_program, type_insurance, premium_amount, manager_email,
                doc_type, channel, main_contract_check, contract_date, policy_date
            )
            VALUES $valuesSql
            """.trimIndent()

        val args = ArrayList<Any?>(subs.size * 14)
        subs.forEach { s ->
            args += s.orderEntity?.orderId
            args += s.policyId
            args += s.policyNumber
            args += s.contractId
            args += s.contractNumber
            args += s.insuranceProgram
            args += s.typeInsurance
            args += s.premiumAmount
            args += s.managerEmail
            args += s.docType
            args += s.channel
            args += s.mainContractCheck
            args += s.contractDate?.let { Timestamp.from(it) }
            args += s.policyDate?.let { Timestamp.from(it) }
        }

        logger.info(LOG_START.format(subs.size))
        jdbcTemplate.update(sql, *args.toTypedArray())
        logger.info(LOG_EXECUTE.format(subs.size))
        logger.info(LOG_DONE.format(subs.size))
    }

    override fun findByOrderId(orderId: UUID?): List<SubOrderEntity?> {
        logger.info(GET_SUB_ORDER_LIST.format(orderId))
        return subOrderRepository.findAllByOrderEntityOrderId(orderId)
    }
}
