package ru.sogaz.site.orderingService.service.order

import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.CreateOrderResult
import ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.util.UUID

interface OrderService {
    /**
     * Универсальный метод для создания заказа.
     * Проверяет данные о заказе, валидирует их и создает запись о заказе в зависимости от версии.
     * @param ru.sogaz.site.orderingService.dto.request.CreateOrderCommand Данные о заказе(Универсальный дто после мапинга разных версий апи)
     * @return Объект CreateOrderResult с информацией о платеже далее мапинг на уровне контроллера в нужный ответ
     */
    fun createOrderInternal(command: CreateOrderCommand): CreateOrderResult

    /**
     * Метод для получения статуса заказа.
     * @param ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus Данные о заказе
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

    fun createRegestryOrder(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): OrderEntity
}