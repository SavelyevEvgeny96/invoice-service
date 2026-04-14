package ru.sogaz.site.orderingService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.dto.request.PayQueryParamsWithRequiredFields
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.payment.CardRegistryService
import ru.sogaz.site.paymentService.dto.request.PayQueryParamsWithRequiredFields
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.service.AuthorizationService
import ru.sogaz.site.paymentService.service.CardRegistryService

@RestController
@Tag(name = "Card registration", description = "Регистрация карт")
@Validated
class CardRegistryController(
    private val cardRegistryService: CardRegistryService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController() {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрация банковской карты",
    )
    @Parameters(
        Parameter(name = "unifiedId", description = "Идентификатор единого профиля клиента", required = true),
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            example = "http://www.sogaz.ru",
            required = true,
        ),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты", required = true),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты", required = true),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            example = "true",
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @GetMapping("/payment/users/{unifiedId}/card")
    fun cardRegistry(
        @PathVariable unifiedId: String,
        @Parameter(hidden = true)
        @Valid payQueryParams: PayQueryParamsWithRequiredFields,
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) token: String,
    ): RedirectView {
        val clientSystem: ClientSystemEntity = authorizationService.checkPermissionByClientId(token)
        return cardRegistryService
            .registry(
                unifiedId = unifiedId,
                payQueryParams = payQueryParams,
                clientId = clientSystem.externalSystemCode,
            ).wrapToRedirectView()
    }
}
