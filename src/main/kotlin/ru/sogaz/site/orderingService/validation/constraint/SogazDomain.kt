package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.URI
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SogazDomainValidator::class])
annotation class SogazDomain(
    val message: String = "Domain is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class SogazDomainValidator : ConstraintValidator<SogazDomain, Any> {
    private val allowedHosts = setOf("sogaz.ru", "sogaz-life.ru", "health-and-care.ru")

    override fun isValid(
        url: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (url == null) return true

        val uri = when (url) {
            is String -> runCatching { URI(url) }.getOrNull()
            is URI -> url
            else -> return true
        } ?: return false

        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase() ?: return false

        return scheme in setOf("http", "https") && allowedHosts.any { host == it || host.endsWith(".$it") }
    }
}
