package ru.sogaz.site.orderingService.dto.data

data class Split<T>(
    val found: List<ParsedData<T>>,
    val missing: List<ParsedData<T>>,
)
