package ru.sogaz.site.orderingService.service.rabbit.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.ApiVersionEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.order.OverdueInvoiceV1Mapper
import ru.sogaz.site.orderingService.mappers.order.OverdueInvoiceV2Mapper
import ru.sogaz.site.orderingService.service.rabbit.OverdueOrderPublisher
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Service
class OverdueOrderPublisherImpl(
    private val overdueInvoiceV2Mapper: OverdueInvoiceV2Mapper,
    private val overdueInvoiceV1Mapper: OverdueInvoiceV1Mapper,
    private val sendMessageProducer: SendMessageProducer,
    @param:Value("\${app.rabbit.payments-exchange}")
    private val exchange: String,
) : OverdueOrderPublisher {
    private val logger = loggerFor(javaClass)

    override fun publish(orders: List<OrderEntity>) {
        orders.forEach { order ->
            val routingKey = order.queueStatusResultName ?: return@forEach

            when (order.versionApi) {
                ApiVersionEnum.V1, null -> publishV1(order, routingKey)
                ApiVersionEnum.V2 -> publishV2(order, routingKey)

            }
        }
    }

    private fun publishV1(
        order: OrderEntity,
        routingKey: String,
    ) {
        val payload = overdueInvoiceV2Mapper.toEvent(order)

        sendMessageProducer.sendMessage(
            routingKey = routingKey,
            payload = payload,
            exchange = exchange,
            orderId = order.orderId,
        )
    }

    private fun publishV2(
        order: OrderEntity,
        routingKey: String,
    ) {
        val payload = overdueInvoiceV1Mapper.toEvent(order)

        sendMessageProducer.sendMessage(
            routingKey = routingKey,
            payload = payload,
            exchange = exchange,
            orderId = order.orderId,
        )
    }
}
