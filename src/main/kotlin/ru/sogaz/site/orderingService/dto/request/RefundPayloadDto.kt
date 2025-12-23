package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RefundPayloadDto(
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID,
    val routingKeyStatus: String? = null,
)
