package ru.sogaz.site.orderingService.apiDoc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.orderingService.apiDoc.response.ForbiddenApiResponse
import ru.sogaz.site.orderingService.apiDoc.response.UnauthorizedApiResponse
import ru.sogaz.site.orderingService.apiDoc.response.ValidationErrorApiResponse
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV1
import ru.sogaz.site.orderingService.dto.request.OrderRequestV1
import ru.sogaz.site.orderingService.dto.response.DataGetOrderStatus
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

interface OrderV1Api {
    @Operation(
        summary = "Создать заказ",
        description = "Создает заявку и возвращает ссылку на оплату.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Успешное создание заказа",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema =
                    Schema(
                        example =
                            "{\n" +
                                "    \"status\": \"SUCCESS\",\n" +
                                "    \"code\": 1101500200,\n" +
                                "    \"traceId\": \"UUID\",\n" +
                                "    \"innerError\": null,\n" +
                                "    \"messagesError\": null,\n" +
                                "    \"responseUuid\": \"UUID\",\n" +
                                "    \"errorsValidate\": null,\n" +
                                "    \"data\": {\n" +
                                "        \"orderId\": \"UUID\",\n" +
                                "        \"url\": \"\${PAYMENT_URL}/payment/pay/\$orderId\"\n" +
                                "    }\n" +
                                "}",
                    ),
            ),
        ],
    )
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @ValidationErrorApiResponse
    @PostMapping("order/create", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createOrder(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на создание заказа",
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = OrderRequestV1::class),
                    examples = [
                        ExampleObject(
                            value =
                                "{\n" +
                                    "    \"orders\":[{               \n" +
                                    "            \"premiumAmount\": \"1.12\",\n" +
                                    "            \"mainContractCheck\": true, \n" +
                                    "            \"operationId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "            \"policyId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "            \"policyNumber\": \"SGZF-0000119500\",\n" +
                                    "            \"contractId\": \"SGZF-0000119500\", \n" +
                                    "            \"policyDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "            \"contractDate\": \"2026-09-15T07:59:01+03:00\", \n" +
                                    "            \"contractNumber\": \"SF-0000119500\",\n" +
                                    "            \"typeInsurance\":\"ОМС\",\n" +
                                    "            \"insuranceProgram\":\"СОГАЗ-Квартира\",\n" +
                                    "            \"docType\": \"Договор\",\n" +
                                    "            \"channel\": \"Адакта\"                                  \n" +
                                    "        },\n" +
                                    "        {              \n" +
                                    "            \"premiumAmount\": \"1.56\",\n" +
                                    "            \"operationId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "            \"policyId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "            \"policyNumber\": \"SGZF-0000119500\",\n" +
                                    "            \"contractId\": \"SGZF-0000119500\", \n" +
                                    "            \"policyDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "            \"contractDate\": \"2026-09-15T07:59:01+03:00\",   \n" +
                                    "            \"contractNumber\": \"SF-0000119500\",\n" +
                                    "            \"typeInsurance\":\"Страхование жизни\",\n" +
                                    "            \"insuranceProgram\":\"ВПМЖ\",\n" +
                                    "            \"docType\": \"Рисунок\",\n" +
                                    "            \"channel\": \"Адакта\"                                   \n" +
                                    "        }],                        \n" +
                                    "    \"recipientEmail\": \"sokolov@sogaz.ru\",\n" +
                                    "    \"saveCard\": true,\n" +
                                    "    \"unifiedId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "    \"orderEndDate\": \"2028-02-15T07:59:01+03:00\", \n" +
                                    "    \"urlToReturn\": \"www.sogaz.ru\",\n" +
                                    "    \"urlToDecline\": \"www.sogaz.ru\",\n" +
                                    "    \"bank\": \"gpb\"\n" +
                                    "}",
                        ),
                    ],
                ),
            ],
        )
        @Valid
        @RequestBody request: OrderRequestV1,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): Response<CreateOrderDataV1>

    @GetMapping("order/status/{orderId}")
    @ApiResponse(responseCode = "200", description = "Успешное получение статуса")
    fun getOrderStatus(
        @PathVariable orderId: UUID,
    ): Response<DataGetOrderStatus>
}
