package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.orderingService.dto.request.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.response.OrdersUserResponse
import ru.sogaz.siter.models.resonses.Response
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@Tag(
    name = "OrdersUser",
    description = "Методы для получения информации о заказах пользователя",
)
interface OrdersUserApi {
    @PostMapping("/v1/orders/ordersuser")
    @Operation(
        summary = "Получить заказы пользователя",
        description = """
            Возвращает список заказов пользователя по параметрам фильтрации.  
            Возможные ошибки:
            - **422** — некорректные параметры запроса (валидация не пройдена);
            - **409** — клиент не найден или нет заказов с указанным статусом.
        """,
        requestBody =
            SwaggerRequestBody(
                required = true,
                description = "Параметры фильтрации заказов пользователя",
                content = [
                    Content(
                        schema = Schema(implementation = OrdersUserRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешный ответ. Заказы найдены.",
                content = [
                    Content(
                        schema = Schema(implementation = OrdersUserResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = """
                    Ошибка валидации данных.  
                    Возвращается, если указаны недопустимые параметры поиска.
                    
                    Пример:
                    {
                      "status": "error",
                      "code": -1101570422,
                      "traceId": "96ece1a7-e634-4b57-86d8-9f89dc5a21fc",
                      "innerError": "UnprocessableEntity",
                      "messagesError": "Не все обязательные данные указаны корректно",
                      "errorsValidate": [
                        {"param": "email", "error": "Значение должно содержать латинские буквы, цифры и спецсимволы"},
                        {"param": "status", "error": "Возможные значения - unpaid или paid"}
                      ],
                      "data": null
                    }
                """,
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "409",
                description = """
                    Ошибка бизнес-логики (Conflict).  
                    Возможные случаи:
                    - Клиент не найден;
                    - Заказы с указанными статусами отсутствуют.
                    
                    Пример:
                    {
                      "status": "error",
                      "code": -1101570409,
                      "traceId": "96ece1a7-e634-4b57-86d8-9f89dc5a21fc",
                      "innerError": "Conflict",
                      "messagesError": "Ошибка получения списка заказов клиента. Клиент с такими данными не найден",
                      "errorsValidate": null,
                      "data": null
                    }
                """,
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервиса",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getClientOrders(
        @Valid @RequestBody request: OrdersUserRequest,
    ): Response<OrdersUserResponse>
}
