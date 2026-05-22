package ru.sogaz.site.orderingService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.beans.factory.annotation.Qualifier
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

class SogazDomainValidator(
    @Qualifier("urlDomainRegex") private val regex: Regex,
) : ConstraintValidator<SogazDomain, Any> {
    override fun isValid(
        url: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (url == null) {
            return true
        }
        return when (url) {
            is String -> regex.matches(url)
            is URI -> regex.matches(url.host)
            else -> true
        }
    }
}
