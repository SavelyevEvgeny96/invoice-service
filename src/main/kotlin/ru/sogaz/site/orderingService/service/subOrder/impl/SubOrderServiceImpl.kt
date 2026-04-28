package ru.sogaz.site.orderingService.service.subOrder.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.service.subOrder.SubOrderService
import java.time.Instant

@Service
class SubOrderServiceImpl(
    private val subOrderDao: SubOrderDao,
) : SubOrderService {
    companion object {
        const val DOC_TYPE_REGISTRY_CARD = "Регистрация карты"
    }

    override fun createSuborder(
        order: OrderEntity,
        clientId: String,
    ): SubOrderEntity =
        SubOrderEntity(
            orderEntity = order,
            docType = DOC_TYPE_REGISTRY_CARD,
            premiumAmount = order.premiumAmount,
            mainContractCheck = true,
            channel = clientId,
            policyId = "",
            contractNumber = "",
            contractId = "",
            createDate = Instant.now(),
        ).run(subOrderDao::save)
}
