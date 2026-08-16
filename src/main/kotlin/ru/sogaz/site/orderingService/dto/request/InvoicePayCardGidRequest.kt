package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class InvoicePayCardGidRequest(
    @field:Schema(description = "Адрес возврата после успешной оплаты")
    val urlToReturnS: String? = null,
    @field:Schema(description = "Адрес возврата после неуспешной оплаты")
    val urlToReturnF: String? = null,
    @field:Schema(description = "Канал продажи")
    val channelSale: String? = null,
    @field:Schema(description = "IP плательщика")
    val payerIP: String? = null,
    @field:NotBlank
    @field:Schema(description = "Идентификатор клиента в ГИД", requiredMode = Schema.RequiredMode.REQUIRED)
    val gid: String,
    @field:NotBlank
    @field:Schema(description = "Идентификатор карты в ГИД", requiredMode = Schema.RequiredMode.REQUIRED)
    val keyCard: String,
    @field:Schema(description = "Деперсонализация страницы оплаты", defaultValue = "false")
    val depersonalization: Boolean = false,
    @field:Schema(description = "Сохранить карту", defaultValue = "false")
    val saveCard: Boolean = false,
)
