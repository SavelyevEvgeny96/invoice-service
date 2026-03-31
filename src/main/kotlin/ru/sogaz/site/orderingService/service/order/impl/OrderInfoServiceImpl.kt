package ru.sogaz.site.orderingService.service.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_INFO_ORDER_NOT_FOUND
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.PaymentOperationDao
import ru.sogaz.site.orderingService.dao.ReceiptDao
import ru.sogaz.site.orderingService.dto.response.CompletedPaymentInfo
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.mappers.order.OrderInfoMapper
import ru.sogaz.site.orderingService.service.order.OrderInfoService
import java.util.UUID

@Service
@Transactional
class OrderInfoServiceImpl(
    private val orderDao: OrderDao,
    private val receiptDao: ReceiptDao,
    private val paymentOperationDao: PaymentOperationDao,
    private val orderInfoMapper: OrderInfoMapper,
) : OrderInfoService {
    override fun getCompletedOrderInfo(orderId: UUID): CompletedPaymentInfo? {
        val order = orderDao.findById(orderId) ?: throw BusinessException(ERROR_CODE_ORDER_INFO_ORDER_NOT_FOUND)
        if (order.isCompleted().not()) {
            return null
        }
        return formOrderInfo(orderId)
    }

    private fun OrderEntity.isCompleted(): Boolean = status in listOf(OrderStatusesEnum.SUCCESS, OrderStatusesEnum.REFUND)

    private fun formOrderInfo(orderId: UUID): CompletedPaymentInfo? {
        val successPayment = paymentOperationDao.findSuccessPaymentByOrderId(orderId)
        val receipts = receiptDao.findReceiptsByOrderId(orderId)
        return orderInfoMapper.mapToCompletedOperationInfo(successPayment, receipts)
    }
}
