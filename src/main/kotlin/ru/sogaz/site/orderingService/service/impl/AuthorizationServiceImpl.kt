package ru.sogaz.site.orderingService.service.impl

import com.auth0.jwt.exceptions.JWTDecodeException
import ru.sogaz.site.jwt.starter.dto.JwtClaims
import ru.sogaz.site.jwt.starter.service.JwtService
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.entity.ClientSystemEntity
import ru.sogaz.site.orderingService.exceptions.ForbiddenBusinessException
import ru.sogaz.site.orderingService.exceptions.UnauthorizedBusinessException
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.AuthorizationService

class AuthorizationServiceImpl(
    private val clientSystemDao: ClientSystemDao,
    private val jwtService: JwtService,
) : AuthorizationService {
    companion object {
        private const val CLIENT_ID = "clientId"
    }

    private val logger = loggerFor(javaClass)

    @Throws(UnauthorizedBusinessException::class, ForbiddenBusinessException::class)
    override fun checkPermissionByClientId(
        authorizationHeader: String?,
        errorCode: Int?,
    ): ClientSystemEntity =
        try {
            jwtService
                .getClaims(authorizationHeader)
                .run { verifyClientSystem(this, errorCode) }
        } catch (ex: JWTDecodeException) {
            logger.error(ex.message)
            throw UnauthorizedBusinessException()
        }

    private fun verifyClientSystem(
        jwtClaims: JwtClaims,
        errorCode: Int? = null,
    ) = jwtClaims
        .getClaim<String>(CLIENT_ID)
        .run(clientSystemDao::findBySystemCode) ?: throw ForbiddenBusinessException(errorCode)
}
