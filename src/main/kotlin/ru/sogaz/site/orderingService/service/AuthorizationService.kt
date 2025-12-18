package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.entity.ClientSystemEntity

interface AuthorizationService {
    fun checkPermissionByClientId(
        authorizationHeader: String?,
        errorCode: Int? = null,
    ): ClientSystemEntity
}
