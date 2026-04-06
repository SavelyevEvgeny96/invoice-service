package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.payment.PaymentOperationMapper
import ru.sogaz.site.orderingService.service.payment.PaymentOperationsService

@Service
class PaymentOperationsServiceImpl(
    private val orderDao: OrderDao,
    private val paymentOperationMapper: PaymentOperationMapper,
    private val paymentOperationDao: PaymentOperationDao,
) : PaymentOperationsService {
    override fun saveOperation(completedPaymentData: CompletedPaymentData): OrderEntity {
        val order = orderDao.findById(completedPaymentData.orderId) ?: throw OrderNotFoundException(completedPaymentData.orderId)
        val paymentOperationHistoryEntity = paymentOperationMapper.fromCompletedPayment(completedPaymentData)
        paymentOperationHistoryEntity.orderEntity = order
        paymentOperationDao.save(paymentOperationHistoryEntity)
        return order
    }
}
