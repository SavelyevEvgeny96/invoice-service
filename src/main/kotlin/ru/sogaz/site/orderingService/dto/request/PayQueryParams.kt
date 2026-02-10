package ru.sogaz.site.orderingService.dto.request

data class PayQueryParams(
    val urlToReturn: String? = null,
    val urlToReturnS: String? = null,
    val urlToReturnF: String? = null,
    val depersonalization: Boolean = false,
)
