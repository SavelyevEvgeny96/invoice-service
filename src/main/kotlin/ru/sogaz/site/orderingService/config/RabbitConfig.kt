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
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.sogaz.site.orderingService.converters.NoOpMessageConverter
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitListenerProps
import ru.sogaz.site.orderingService.properties.RabbitProps
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Configuration
class RabbitConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProps,
    private val propsListener: RabbitListenerProps,
) {
    companion object {
        private const val CONFIRMED_LOG = " Сообщение подтверждено брокером: orderId=%s"
        private const val N_ACK_LOG = " Сообщение отклонено брокером: orderId=%s, причина=%s"
        private const val RETURNED_LOG = " Сообщение возвращено брокером: %s, reply=%s"
    }

    private val logger = loggerFor(RabbitConfig::class.java)
    private val confirmed = ConcurrentHashMap<UUID, Boolean>()
    private val errors = ConcurrentHashMap<UUID, String?>()

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter,
    ): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter

        template.setConfirmCallback { correlation, ack, cause ->
            val id = correlation?.id ?: return@setConfirmCallback
            val orderId = UUID.fromString(id)

            if (ack) {
                confirmed[orderId] = true
                logger.debug(CONFIRMED_LOG.format(orderId))
            } else {
                errors[orderId] = cause
                logger.error(N_ACK_LOG.format(orderId, cause))
            }
        }

        template.setReturnsCallback { returned ->
            logger.error(RETURNED_LOG.format(returned.message, returned.replyText))
        }

        return template
    }

    @Bean
    fun confirmedMap(): ConcurrentHashMap<UUID, Boolean> = confirmed

    @Bean
    fun errorsMap(): ConcurrentHashMap<UUID, String?> = errors

    @Bean(name = ["ordersExchange"])
    fun ordersExchange(): TopicExchange = TopicExchange(props.ordersExchange, true, false)

    @Bean(name = ["paymentsExchange"])
    fun paymentsExchange(): TopicExchange = TopicExchange(props.paymentsExchange, true, false)

    // Основная очередь заказов с DLQ
    @Bean(name = ["ordersQueue"])
    fun ordersQueue(): Queue =
        QueueBuilder
            .durable(props.queueOrder)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "${props.queueOrder}.dlq")
            .build()

    @Bean(name = ["ordersDlq"])
    fun ordersDlq(): Queue = QueueBuilder.durable("${props.queueOrder}.dlq").build()

    @Bean(name = ["paymentsQueue"])
    fun paymentsQueue(): Queue = QueueBuilder.durable(props.queuePayment).build()

    @Bean
    fun ordersBinding(
        @Qualifier("ordersQueue") queue: Queue,
        @Qualifier("ordersExchange")exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyOrder)

    @Bean
    fun paymentsBinding(
        @Qualifier("paymentsQueue") queue: Queue,
        @Qualifier("paymentsExchange")exchange: TopicExchange,
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
            setChannelTransacted(true)
            setDefaultRequeueRejected(false)

            setMessageConverter(noOpMessageConverter)
        }
}
