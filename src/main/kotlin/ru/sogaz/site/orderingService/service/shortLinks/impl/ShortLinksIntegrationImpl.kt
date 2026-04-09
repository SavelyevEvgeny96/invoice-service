package ru.sogaz.site.orderingService.service.shortLinks.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.service.shortLinks.ShortLinksIntegration
import ru.sogaz.site.shortlinks.client.api.ShortLinkControllerApi
import ru.sogaz.site.shortlinks.client.model.ResponseData
import ru.sogaz.site.shortlinks.client.model.ShortLinkRequest

@Service
class ShortLinksIntegrationImpl(
    private val shortLinkControllerApi: ShortLinkControllerApi,
) : ShortLinksIntegration {
    override fun createShortLink(request: ShortLinkRequest): ResponseData? = shortLinkControllerApi.createShortLink(request)
}
