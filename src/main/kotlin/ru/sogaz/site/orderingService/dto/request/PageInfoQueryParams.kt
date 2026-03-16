package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class PageInfoQueryParams(
    @field:Parameter(description = "Ссылка для возврата", example = "http://www.sogaz.ru")
    val urlToReturn: String? = null,
    @field:Parameter(description = "Ссылка для редиректа после успешной оплаты")
    val urlToReturnS: String? = null,
    @field:Parameter(description = "Ссылка для редиректа после неуспешной оплаты")
    val urlToReturnF: String? = null,
    @field:Parameter(description = "Флаг необходимости анонимизированной оплаты")
    @field:Schema(defaultValue = "false")
    val depersonalization: Boolean = false,
    @field:Parameter(description = "Флаг необходимости сохранения карты")
    @field:Schema(defaultValue = "false")
    val saveCard: Boolean = false,
    @field:Parameter(description = "Уникальный идентификатор клиента в ЕПК")
    val unifiedId: String? = null,
)