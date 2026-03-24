package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.data.DataOrder
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface OrderService {
    /**
     * Метод для создания заказа.
     * Проверяет данные о заказе, валидирует их и создает запись о заказе.
     * @param CreateOrderCommand Данные о заказе(Универсальный дто после мапинга разных версий апи)
     * @return Объект Response с информацией о платеже
     */
    fun createOrder(command: CreateOrderCommand): Response<DataOrder>

    /**
     * Метод для получения статуса заказа.
     * @param DataGetOrderStatus Данные о заказе
     * @return Объект DataGetOrderStatus со статусом заказа
     */
    fun getOrderStatus(orderId: UUID): DataGetOrderStatus

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
