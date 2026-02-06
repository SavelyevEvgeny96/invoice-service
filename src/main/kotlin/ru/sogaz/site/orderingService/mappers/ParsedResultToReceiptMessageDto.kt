package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.request.SellRefundMessageDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.math.BigDecimal
import java.time.OffsetDateTime

@Mapper(
    componentModel = "spring",
    imports = [OffsetDateTime::class, BigDecimal::class],
)
interface ParsedResultToReceiptMessageDto {
    @Mapping(target = "metaInfo", source = "ignored", qualifiedByName = ["metaInfo"])
    @Mapping(target = "type", constant = "sell_refund")
    @Mapping(target = "paymentMethod", constant = "full_payment")
    @Mapping(target = "paymentObject", constant = "service")
    @Mapping(target = "system", constant = "Atol")
    @Mapping(target = "version", constant = "v4")
    @Mapping(target = "depersonalization", constant = "false")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "total", source = "order.premiumAmount")
    @Mapping(target = "client", source = "order")
    @Mapping(target = "items", source = "subOrder", qualifiedByName = ["items"])
    @Mapping(target = "payments", source = "order", qualifiedByName = ["payments"])
    @Mapping(target = "vat", expression = "java(new SellRefundMessageDto.Vat(\"none\"))")
    @Mapping(target = "channel", source = "subOrder", qualifiedByName = ["channel"])
    @Mapping(target = "product", source = "subOrder", qualifiedByName = ["product"])
    fun toDto(
        order: OrderEntity,
        subOrder: SubOrderEntity,
    ): SellRefundMessageDto

    @Mapping(target = "email", source = "recipientEmail")
    @Mapping(target = "phone", source = "recipientPhone")
    @Mapping(target = "name", source = "policyholder")
    @Mapping(target = "userId", source = "recipientUserId")
    fun toClient(order: OrderEntity): SellRefundMessageDto.Client

    @Named("items")
    fun items(subOrder: SubOrderEntity): List<SellRefundMessageDto.Item> =
        listOf(
            SellRefundMessageDto.Item(
                name = "Страховая премия по договору страхования №${subOrder.contractNumber}",
                price = subOrder.premiumAmount,
                quantity = BigDecimal.ONE,
                sum = subOrder.premiumAmount,
            ),
        )

    @Named("payments")
    fun payments(order: OrderEntity): List<SellRefundMessageDto.Payment> =
        listOf(
            SellRefundMessageDto.Payment(
                type = "1",
                sum = order.premiumAmount,
            ),
        )

    @Named("metaInfo")
    fun metaInfo(): MetaInfoOrder =
        MetaInfoOrder(
            eventTimeIso = OffsetDateTime.now().toInstant(),
            author = "order.service",
            routingKey = "payment.receipt.create",
        )

    @Named("channel")
    fun channel(subOrder: SubOrderEntity): String = "online"

    @Named("product")
    fun product(subOrder: SubOrderEntity): String = "insurance"
}
