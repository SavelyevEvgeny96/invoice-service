package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.payment.CompletedPaymentMapper
import ru.sogaz.site.orderingService.service.order.OrderStatusService
import java.util.UUID

@Service
class OrderStatusServiceImpl(
    private val orderDao: OrderDao,
    private val completedPaymentMapper: CompletedPaymentMapper,
) : OrderStatusService {
    @Transactional(rollbackFor = [Exception::class])
    override fun updatePaidOrder(completedPaymentData: CompletedPaymentData): OrderEntity =
        completedPaymentData.orderId
            .run(::findOrderOrThrow)
            .fillByPaymentData(completedPaymentData)
            .run(orderDao::save)

    private fun findOrderOrThrow(orderId: UUID): OrderEntity = orderDao.findById(orderId) ?: throw OrderNotFoundException(orderId)

    private fun OrderEntity.fillByPaymentData(completedPaymentData: CompletedPaymentData): OrderEntity =
        completedPaymentMapper.fillPaidOrder(this, completedPaymentData)
}
