package ru.sogaz.site.orderingService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.dao.ReceiptDao
import ru.sogaz.site.orderingService.entity.ReceiptEntity
import ru.sogaz.site.orderingService.repository.ReceiptRepository

@Repository
class ReceiptDaoImpl(
    private val receiptRepository: ReceiptRepository,
) : ReceiptDao {
    override fun save(receipt: ReceiptEntity): ReceiptEntity = receiptRepository.save(receipt)
}
