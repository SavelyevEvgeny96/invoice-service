package ru.sogaz.site.orderingService.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.orderingService.apiDoc.CardRegistryApi
import ru.sogaz.site.orderingService.dto.request.PayQueryParamsWithRequiredFields
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import ru.sogaz.site.orderingService.service.AuthorizationService
import ru.sogaz.site.orderingService.service.payment.CardRegistryService

@RestController
@Validated
class CardRegistryController(
    private val cardRegistryService: CardRegistryService,
    private val authorizationService: AuthorizationService,
) : WrapResponseController(),
    CardRegistryApi {
    override fun cardRegistry(
        unifiedId: String,
        payQueryParams: PayQueryParamsWithRequiredFields,
        token: String,
    ): RedirectView {
        val clientSystem: ClientSystemEntity = authorizationService.checkPermissionByClientId(token)
        return cardRegistryService
            .registry(
                unifiedId = unifiedId,
                payQueryParams = payQueryParams,
                clientId = clientSystem.externalSystemCode,
            ).toRedirectView()
    }
}
