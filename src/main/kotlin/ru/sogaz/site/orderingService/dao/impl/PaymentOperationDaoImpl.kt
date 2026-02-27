package ru.sogaz.site.orderingService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.entity.PaymentOperationEntity
import ru.sogaz.site.orderingService.repository.PaymentOperationRepository

@Repository
class PaymentOperationDaoImpl(
    private val paymentOperationRepository: PaymentOperationRepository,
) : PaymentOperationDao {
    override fun save(paymentOperation: PaymentOperationEntity): PaymentOperationEntity = paymentOperationRepository.save(paymentOperation)
}
