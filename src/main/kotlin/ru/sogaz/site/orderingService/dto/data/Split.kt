package ru.sogaz.site.orderingService.dto.data

data class Split<T>(
    val found: List<Parsed<T>>,
    val missing: List<Parsed<T>>,
)