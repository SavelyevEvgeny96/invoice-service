package ru.sogaz.site.orderingService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.mappers.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.site.orderingService.service.PaymentEventProducer
import ru.sogaz.site.orderingService.service.impl.BuildBatchConsumerServiceImpl
import ru.sogaz.site.orderingService.service.impl.OrderBatchConsumerImpl
import ru.sogaz.site.orderingService.service.impl.OrdersUserServiceImpl
import ru.sogaz.site.orderingService.service.impl.PaymentEventProducerImpl

@Configuration
class ServiceConfig {
    @Bean
    fun buildBatchConsumerConfig(
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
        props: RabbitProps,
        orderMapper: OrderMapper,
        paymentEventMapper: PaymentEventMapper,
    ): BuildBatchConsumerService =
        BuildBatchConsumerServiceImpl(
            orderDao = orderDao,
            subOrderDao = subOrderDao,
            props = props,
            orderMapper = orderMapper,
            paymentEventMapper = paymentEventMapper,
        )

    @Bean
    fun orderBatchConsumerConfig(
        buildBatchConsumerService: BuildBatchConsumerService,
        paymentProducer: PaymentEventProducer,
        messageConverter: MessageConverter,
        objectMapper: ObjectMapper,
    ): OrderBatchConsumer =
        OrderBatchConsumerImpl(
            buildBatchConsumerService = buildBatchConsumerService,
            paymentProducer = paymentProducer,
            messageConverter = messageConverter,
            objectMapper = objectMapper,
        )

    @Bean
    fun paymentEventProducerConfig(
        rabbit: RabbitTemplate,
        props: RabbitProps,
    ): PaymentEventProducer = PaymentEventProducerImpl(rabbit = rabbit, props = props)

    @Bean
    fun orderServiceConfig(
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
    ): OrdersUserService = OrdersUserServiceImpl(orderDao = orderDao, subOrderDao = subOrderDao)
}
