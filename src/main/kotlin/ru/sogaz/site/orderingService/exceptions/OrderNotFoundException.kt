package ru.sogaz.site.orderingService.exceptions

import java.util.UUID

const val NOT_FOUND_ORDER_ERROR_MESSAGE = "Не найден заказ по id: "

class OrderNotFoundException(
    id: UUID,
) : RuntimeException("$NOT_FOUND_ORDER_ERROR_MESSAGE$id")
