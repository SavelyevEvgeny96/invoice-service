package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.OrderRequest
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface OrderService {
    /**
     * Метод для создания заказа.
     * Проверяет данные о заказе, валидирует их и создает запись о заказе.
     * @param orderRequest Данные о заказе(содержит внутри лист PaymentRequest)
     * @return Объект Response с информацией о платеже
     */
    fun createOrder(orderRequest: OrderRequest): Response<DataOrder>

    /**
     * Метод регистрации платежной ссылки для оплаты заказа картой
     * @param orderId идентификатор заказа в базе
     * @param payQueryParams query параметры оплаты
     * @return Объект хранящий ссылку на оплату
     */
    fun payCard(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): PaymentPage

    /**
     * Метод регистрации платежной ссылки для оплаты заказа по СБП
     * @param orderId идентификатор заказа в базе
     * @param payQueryParams query параметры оплаты
     * @return Объект хранящий ссылку на оплату
     */
    fun paySbp(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): PaymentPage
}
