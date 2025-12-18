package ru.sogaz.site.orderingService.dto.request

import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import java.util.UUID

class RefundPayloadDto(
    val metaInfo: MetaInfoOrder,
    val orderId: UUID
)