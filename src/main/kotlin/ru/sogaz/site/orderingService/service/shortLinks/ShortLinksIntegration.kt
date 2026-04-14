package ru.sogaz.site.orderingService.service.shortLinks
import ru.sogaz.site.shortlinks.client.model.ResponseData
import ru.sogaz.site.shortlinks.client.model.ShortLinkRequest

interface ShortLinksIntegration {
    fun createShortLink(request: ShortLinkRequest): ResponseData?
}
