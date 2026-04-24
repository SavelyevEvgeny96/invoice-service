package ru.sogaz.site.orderingService.controller

import org.springframework.web.servlet.view.RedirectView

abstract class WrapResponseController {
    fun String.toRedirectView(): RedirectView = RedirectView(this)
}
