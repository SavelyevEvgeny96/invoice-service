package ru.sogaz.site.orderingService.apiDoc.response

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType

@ApiResponse(
    responseCode = "422",
    description = "Ошибка валидации данных",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema =
                Schema(
                    example =
                        "{\n" +
                            "    \"status\": \"error\",\n" +
                            "    \"code\": \"-11015**422\",\n" +
                            "    \"traceId\": \"UUID\",\n" +
                            "    \"innerError\": \"UNPROCESSABLE_ENTITY\",\n" +
                            "    \"messagesError\": \"Не все обязательные данные указаны корректно.\",\n" +
                            "    \"responseUuid\": \"UUID\",\n" +
                            "    \"errorsValidate\": [\n" +
                            "        {\n" +
                            "            \"param\": \"paramName\",\n" +
                            "            \"error\": \"Сообщение об ошибке\"\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"param\": \"SomeList[1].paramName\",\n" +
                            "            \"error\": \"Сообщение об ошибке\"\n" +
                            "        }\n" +
                            "    ],\n" +
                            "    \"data\": null\n" +
                            "}",
                ),
        ),
    ],
)
annotation class ValidationErrorApiResponse
