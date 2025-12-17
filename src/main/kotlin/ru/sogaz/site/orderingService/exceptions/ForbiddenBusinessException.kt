package ru.sogaz.site.paymentService.exceptions

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_FORBIDDEN

class ForbiddenBusinessException(
    errorCode: Int?,
) : BusinessException(errorCode ?: CODE_ERROR_FORBIDDEN)
