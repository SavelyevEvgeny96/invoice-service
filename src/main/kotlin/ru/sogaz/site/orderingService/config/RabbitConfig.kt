package ru.sogaz.site.orderingService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.sogaz.site.orderingService.converters.NoOpMessageConverter
import ru.sogaz.site.orderingService.properties.RabbitListenerProps
import ru.sogaz.site.orderingService.properties.RabbitProps

@Configuration
class RabbitConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProps,
    private val propsListener: RabbitListenerProps,
) {
    @Bean(name = ["ordersExchange"])
    fun ordersExchange(): TopicExchange = TopicExchange(props.ordersExchange, true, false)

    @Bean(name = ["paymentsExchange"])
    fun paymentsExchange(): TopicExchange = TopicExchange(props.paymentsExchange, true, false)

    // Основная очередь заказов с DLQ (classic)
    @Bean(name = ["ordersQueue"])
    fun ordersQueue(): Queue =
        QueueBuilder
            .durable(props.queueOrder)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "${props.queueOrder}.dlq")
            .build()

    @Bean(name = ["ordersDlq"])
    fun ordersDlq(): Queue =
        QueueBuilder
            .durable("${props.queueOrder}.dlq")
            .build()

    @Bean(name = ["paymentStatusRefundQueue"])
    fun paymentStatusRefundQueue(): Queue =
        QueueBuilder
            .durable(props.queuePaymentStatusRefund)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "${props.queuePaymentStatusRefund}.dlq")
            .build()

    @Bean(name = ["paymentStatusRefundQueueDlq"])
    fun paymentStatusRefundQueueDlq(): Queue =
        QueueBuilder
            .durable("${props.queuePaymentStatusRefund}.dlq")
            .build()

    // Основная очередь возвратов с DLQ (classic)
    @Bean(name = ["ordersRefundQueue"])
    fun ordersRefundQueue(): Queue =
        QueueBuilder
            .durable(props.queueOrderRefund)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "${props.queueOrderRefund}.dlq")
            .build()

    @Bean(name = ["ordersRefundDlq"])
    fun ordersRefundDlq(): Queue =
        QueueBuilder
            .durable("${props.queueOrderRefund}.dlq")
            .build()

    @Bean(name = ["paymentsQueue"])
    fun paymentsQueue(): Queue =
        QueueBuilder
            .durable(props.queuePayment)
            .build()

    @Bean
    fun ordersRefundBinding(
        @Qualifier("ordersQueue") queue: Queue,
        @Qualifier("ordersExchange") exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyOrder)

    @Bean
    fun ordersBinding(
        @Qualifier("ordersRefundQueue") queue: Queue,
        @Qualifier("ordersExchange") exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyOrderRefund)

    @Bean
    fun paymentsBinding(
        @Qualifier("paymentsQueue") queue: Queue,
        @Qualifier("paymentsExchange") exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyPayment)

    @Bean
    @Primary
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean("batchContainerFactory")
    fun batchContainerFactory(noOpMessageConverter: NoOpMessageConverter): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setBatchListener(true)
            setConsumerBatchEnabled(true)
            setDeBatchingEnabled(true)
            setBatchSize(propsListener.batchSize)
            setPrefetchCount(propsListener.prefetch)
            setConcurrentConsumers(propsListener.concurrency)
            setMaxConcurrentConsumers(propsListener.maxConcurrency)
            setAcknowledgeMode(AcknowledgeMode.MANUAL)
            setChannelTransacted(false)
            setDefaultRequeueRejected(false)

            setMessageConverter(noOpMessageConverter)
        }
}
