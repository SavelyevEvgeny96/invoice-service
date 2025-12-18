package ru.sogaz.site.orderingService.apiDoc.response

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType

@ApiResponse(
    responseCode = "403",
    description = "Доступ запрещен",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema =
                Schema(
                    example =
                        "{\n" +
                            "    \"status\": \"ERROR\",\n" +
                            "    \"code\": \"-11015**403\",\n" +
                            "    \"traceId\": \"UUID\",\n" +
                            "    \"innerError\": null,\n" +
                            "    \"messagesError\": \"Вам запрещен доступ к запрашиваемому ресурсу\",\n" +
                            "    \"responseUuid\": \"UUID\",\n" +
                            "    \"errorsValidate\": null,\n" +
                            "    \"data\": null\n" +
                            "    }\n" +
                            "}",
                ),
        ),
    ],
)
annotation class ForbiddenApiResponse
