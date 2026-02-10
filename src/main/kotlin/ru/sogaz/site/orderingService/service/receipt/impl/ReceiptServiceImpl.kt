package ru.sogaz.site.orderingService.service.receipt.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.CompletedPaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.ReceiptState
import ru.sogaz.site.orderingService.exceptions.OrderNotFoundException
import ru.sogaz.site.orderingService.mappers.receipt.ReceiptMapper
import ru.sogaz.site.orderingService.service.receipt.ReceiptClient
import ru.sogaz.site.orderingService.service.receipt.ReceiptService
import java.util.UUID

@Service
@Transactional(rollbackFor = [Exception::class])
class ReceiptServiceImpl(
    private val orderDao: OrderDao,
    private val receiptMapper: ReceiptMapper,
    private val receiptClient: ReceiptClient,
) : ReceiptService {
    override fun sendReceipt(completedPaymentData: CompletedPaymentData): OrderEntity {
        val order = findOrderByIdOrThrow(completedPaymentData.orderId)
        sendReceipt(order, completedPaymentData)
        order.receiptState = ReceiptState.REQUESTED
        return orderDao.save(order)
    }

    private fun findOrderByIdOrThrow(orderId: UUID): OrderEntity = orderDao.findById(orderId) ?: throw OrderNotFoundException(orderId)

    private fun sendReceipt(
        order: OrderEntity,
        completedPaymentData: CompletedPaymentData,
    ) {
        val receiptCreateRequest = receiptMapper.mapFromPaymentToReceiptCreateRequest(order, completedPaymentData)
        receiptClient.sendReceiptToQueue(receiptCreateRequest)
    }
}
