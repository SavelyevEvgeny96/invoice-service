package ru.sogaz.site.orderingService.converters

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

/**
 * Конвертер-заглушка (No-Op), который возвращает оригинальный Message без изменений.
 *
 * Используется в batch listener'ах, где нужно получить исходные сообщения без десериализации.
 */
@Component
class NoOpMessageConverter : MessageConverter {
    override fun toMessage(
        p0: Any,
        p1: MessageProperties,
    ): Message = throw UnsupportedOperationException("Метод toMessage() не используется в NoOpMessageConverter")

    override fun fromMessage(message: Message): Any = message
}
