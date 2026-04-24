package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
open class PayQueryParams(
    @field:Parameter(description = "Ссылка для возврата", example = "http://www.sogaz.ru")
    open val urlToReturn: String? = null,
    @field:Parameter(description = "Ссылка для редиректа после успешной оплаты")
    open val urlToReturnS: String? = null,
    @field:Parameter(description = "Ссылка для редиректа после неуспешной оплаты")
    open val urlToReturnF: String? = null,
    @field:Parameter(description = "Флаг необходимости анонимизированной оплаты")
    @field:Schema(defaultValue = "false")
    open val depersonalization: Boolean = false,
    @field:Parameter(description = "Канал продажи")
    val channelSale: String? = null,
    @field:Parameter(description = "IP пользователя, который совершает оплату")
    val payerIP: String? = null,
)
