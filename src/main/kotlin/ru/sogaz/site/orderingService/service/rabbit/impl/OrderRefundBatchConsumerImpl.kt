package ru.sogaz.site.orderingService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.data.ParsedResult
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.dto.data.RefundResponseDto
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mappers.ParsedResultToReceiptMessageDto
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.impl.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_REFUND_PATTERN
import ru.sogaz.site.orderingService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.rabbit.OrderRefundBatchConsumer
import ru.sogaz.site.orderingService.service.rabbit.SendMessageProducer

@Service
class OrderRefundBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProps,
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val parsedResultToReceiptMessageDto: ParsedResultToReceiptMessageDto,
) : OrderRefundBatchConsumer {
    companion object {

    }


    private val logger = loggerFor(OrderRefundBatchConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-invoice-reversal}"])
    override fun handleBatchRefundCreated(
        @Payload refundEvent: RefundPayloadDto
    ) {
        val started = System.nanoTime()



    }

}
