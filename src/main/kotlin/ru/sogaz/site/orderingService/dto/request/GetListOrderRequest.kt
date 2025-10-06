package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import ru.sogaz.site.orderingService.enums.Condition
import ru.sogaz.site.orderingService.enums.OrdersStatusIsPaid
import ru.sogaz.site.orderingService.enums.SearchName

data class GetListOrderRequest(
    @field:NotNull
    val status: OrdersStatusIsPaid?,

    @field:NotNull
    val searchName: SearchName?,
    val userId: String? = null,

    // обязателен, если searchName=gdId
    val gdId: String? = null,

    // валиден, если searchName=emailOrPhone
    @field:Email
    val email: String? = null,

    // валиден, если searchName=emailOrPhone
    @field:Pattern(
        regexp = """^\+?\d{5,20}${'$'}"""
    )
    val phone: String? = null,

    // обязателен только если одновременно есть email и phone
    val condition: Condition? = null
)
