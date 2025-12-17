package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.siter.models.resonses.Response

interface OrderService {
    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param orderRequest Данные о заказе(содержит внутри лист PaymentRequest)
     * @return Объект Response с информацией о платеже
     */
    fun createOrder(orderRequest: OrderRequest): Response<DataOrder>
}
