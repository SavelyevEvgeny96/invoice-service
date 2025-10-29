
package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.request.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.request.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.time.ZoneOffset

@Mapper(componentModel = "spring")
abstract class PaymentEventMapper {
    @Mapping(target = "timestamp", source = "nowIso")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "data", source = "order", qualifiedByName = ["mapPaymentData"])
    abstract fun toPaymentEvent(
        order: OrderEntity,
        nowIso: String,
        eventType: String,
    ): PaymentCreatedEvent

    @Named("mapPaymentData")
    fun toPaymentData(order: OrderEntity): PaymentData =
        PaymentData(
            recurrent = order.recurrent ?: false,
            orderId = order.orderId,
            premiumAmount = order.premiumAmount,
            saveCard = order.saveCard,
            keyCard = order.keyCard,
            recipientEmail = order.recipientEmail,
            recipientPhone = order.recipientPhone,
            dateCreate = order.createDate?.atOffset(ZoneOffset.UTC)?.toString(),
            dateEnd = order.paymentEndDate?.atOffset(ZoneOffset.UTC)?.toString(),
        )
}
