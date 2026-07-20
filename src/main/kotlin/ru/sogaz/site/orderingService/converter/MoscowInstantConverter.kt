package ru.sogaz.site.orderingService.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Converter(autoApply = false)
class MoscowInstantConverter : AttributeConverter<Instant, LocalDateTime> {
    private val zoneId = ZoneId.of("Europe/Moscow")

    override fun convertToDatabaseColumn(attribute: Instant?): LocalDateTime? = attribute?.atZone(zoneId)?.toLocalDateTime()

    override fun convertToEntityAttribute(dbData: LocalDateTime?): Instant? = dbData?.atZone(zoneId)?.toInstant()
}
