package ru.sogaz.site.orderingService.apiDoc.response

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType

@ApiResponse(
    responseCode = "401",
    description = "Неавторизованный запрос",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema =
                Schema(
                    example =
                        "{\n" +
                            "    \"status\": \"ERROR\",\n" +
                            "    \"code\": \"-11015**401\",\n" +
                            "    \"traceId\": \"UUID\",\n" +
                            "    \"innerError\": null,\n" +
                            "    \"messagesError\": \"Ваш запрос не авторизован\",\n" +
                            "    \"responseUuid\": \"UUID\",\n" +
                            "    \"errorsValidate\": null,\n" +
                            "    \"data\": null\n" +
                            "    }\n" +
                            "}",
                ),
        ),
    ],
)
annotation class UnauthorizedApiResponse
