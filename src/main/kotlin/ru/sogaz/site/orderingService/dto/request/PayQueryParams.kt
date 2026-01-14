package ru.sogaz.site.orderingService.dto.request

import java.net.URI

data class PayQueryParams(
    val urlToReturn: URI? = null,
    val urlToReturnS: URI? = null,
    val urlToReturnF: URI? = null,
    val depersonalization: Boolean = false,
)
