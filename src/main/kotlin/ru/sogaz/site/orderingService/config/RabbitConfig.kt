package ru.sogaz.site.orderingService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
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

    @Bean
    fun concurrentContainerFactory(
        connectionFactory: ConnectionFactory,
        jacksonMessageConverter: MessageConverter,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(jacksonMessageConverter)
            setChannelTransacted(true)
            setConcurrentConsumers(propsListener.consumers)
            setMaxConcurrentConsumers(propsListener.maxConsumers)
            setStopConsumerMinInterval(propsListener.stopConsumerMinIntervalMs)
        }
}
