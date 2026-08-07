package ru.sogaz.site.orderingService.apiDoc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import ru.sogaz.site.orderingService.apiDoc.response.ForbiddenApiResponse
import ru.sogaz.site.orderingService.apiDoc.response.UnauthorizedApiResponse
import ru.sogaz.site.orderingService.apiDoc.response.ValidationErrorApiResponse
import ru.sogaz.site.orderingService.dto.data.CreateOrderDataV2
import ru.sogaz.site.orderingService.dto.request.OrderRequestV2
import ru.sogaz.siter.models.resonses.Response

@RequestMapping("/v2")
interface InvoiceV2Api {
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
    @PostMapping("invoice/create", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createOrder(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на создание заказа",
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = OrderRequestV2::class),
                    examples = [
                        ExampleObject(
                            value =
                                "{\n" +
                                    "  \"invoices\": [\n" +
                                    "    {\n" +
                                    "      \"premium\": 1.12,\n" +
                                    "      \"policyId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "      \"policyNumber\": \"SGZF-0000119500\",\n" +
                                    "      \"policyDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "      \"agreementNumber\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "      \"agreementId\": \"SF-0000119500\",\n" +
                                    "      \"agreementDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "      \"typeOperation\": \"PAYMENT_MAIN_AGREEMENT\",\n" +
                                    "      \"insuranceKind\": \"DMSFL\",\n" +
                                    "      \"program\": \"СОГАЗ-Квартира\",\n" +
                                    "      \"managerEmail\": \"manager@sogaz.ru\",\n" +
                                    "      \"channel\": \"ADAKTA\"\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"premium\": 1.56,\n" +
                                    "      \"policyId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "      \"policyNumber\": \"SGZF-0000119501\",\n" +
                                    "      \"policyDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "      \"agreementNumber\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f2\",\n" +
                                    "      \"agreementId\": \"SF-0000119501\",\n" +
                                    "      \"agreementDate\": \"2026-09-15T07:59:01+03:00\",\n" +
                                    "      \"typeOperation\": \"PAYMENT_ADDITIONAL_AGREEMENT\",\n" +
                                    "      \"insuranceKind\": \"VPMG\",\n" +
                                    "      \"program\": \"ВПМЖ\",\n" +
                                    "      \"managerEmail\": \"manager@sogaz.ru\",\n" +
                                    "      \"channel\": \"ADAKTA\"\n" +
                                    "    }\n" +
                                    "  ],\n" +
                                    "  \"email\": \"sokolov@sogaz.ru\",\n" +
                                    "  \"phoneNumber\": \"+79991234567\",\n" +
                                    "  \"unifiedId\": \"294c86cb-faf1-16ed-8e88-0894ef6d43f1\",\n" +
                                    "  \"saveCard\": true,\n" +
                                    "  \"invoiceEndDate\": \"2028-02-15T07:59:01+03:00\",\n" +
                                    "  \"urlToReturn\": \"https://www.sogaz.ru/success\",\n" +
                                    "  \"urlToDecline\": \"https://www.sogaz.ru/decline\",\n" +
                                    "  \"externalId\": \"ext-123456\",\n" +
                                    "  \"paymentMethodList\": [\"CARD\", \"SBP\"],\n" +
                                    "  \"payerFio\": {\n" +
                                    "    \"lastName\": \"Иванов\",\n" +
                                    "    \"firstName\": \"Иван\",\n" +
                                    "    \"middleName\": \"Иванович\"\n" +
                                    "  },\n" +
                                    "  \"checkUrlReturn\": true,\n" +
                                        "  \"checkPaymentInformation\": true,\n" +
                                    "  \"bankQR\": \"GPB\",\n" +
                                    "  \"typePaymentOperation\": \"PAYMENT_CONTRACT\",\n" +
                                    "  \"accountCrossId\": \"account-cross-123\"\n" +
                                    "}",
                        ),
                    ],
                ),
            ],
        )
        @Valid
        @RequestBody request: OrderRequestV2,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): Response<CreateOrderDataV2>
}
