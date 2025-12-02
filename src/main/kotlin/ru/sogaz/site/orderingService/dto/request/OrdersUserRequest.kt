package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.OrdersStatusIsPaidEnum
import ru.sogaz.site.orderingService.enums.OrdersUserConditionEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchNameEnum
import ru.sogaz.site.orderingService.validation.constraint.EmailValid
import ru.sogaz.site.orderingService.validation.constraint.Phone
import ru.sogaz.site.orderingService.validation.constraint.ValidateOrdersUserRequest

/**
 * Запрос на получение списка заказов клиента (POST /v1/orders/ordersuser)
 */
@ValidateOrdersUserRequest
data class OrdersUserRequest(
    /**
     * Статус заказа (обязательный)
     * Возможные значения: unpaid, paid
     */
    @field:NotNull(message = "{validation.ordersUserRequest.status.notNull}")
    val status: OrdersStatusIsPaidEnum? = null,
    /**
     * Параметр для поиска (обязательный)
     * Возможные значения: userId, gdId, emailOrPhone
     */
    @field:NotNull(message = "{validation.ordersUserRequest.searchName.notNull}")
    val searchName: OrdersUserSearchNameEnum? = null,
    /** Идентификатор личного кабинета пользователя – обязателен, если searchName=userId */
    val userId: String? = null,
    /** Идентификатор золотой карточки клиента – обязателен, если searchName=unifiedId */
    val unifiedId: String? = null,
    /** Email – используется, если searchName=emailOrPhone */
    @field:EmailValid(message = "{validation.ordersUserRequest.email.invalid}")
    val email: String? = null,
    /** Телефон – используется, если searchName=emailOrPhone */
    @field:Phone(message = "{validation.ordersUserRequest.phone.invalid}")
    val phone: String? = null,
    /**
     * Логическое условие для email/phone – обязателен, если searchName=emailOrPhone
     * Возможные значения: or, and
     */
    val condition: OrdersUserConditionEnum? = null,
)
