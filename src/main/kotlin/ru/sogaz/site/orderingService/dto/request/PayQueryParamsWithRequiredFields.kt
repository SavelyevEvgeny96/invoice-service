package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import ru.sogaz.site.orderingService.validation.constraint.SogazDomain
import javax.validation.constraints.NotNull

data class PayQueryParamsWithRequiredFields(
    @param:Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты")
    @field:SogazDomain
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturn: String?,
    @param:Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты")
    @field:SogazDomain
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturnS: String?,
    @param:Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты")
    @field:SogazDomain
    @field:NotNull(message = "urlToReturn is required")
    override val urlToReturnF: String?,
    override val depersonalization: Boolean = false,
) : PayQueryParams()
