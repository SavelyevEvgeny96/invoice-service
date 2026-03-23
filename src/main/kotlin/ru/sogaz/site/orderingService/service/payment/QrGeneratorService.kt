package ru.sogaz.site.orderingService.service.payment

import ru.sogaz.site.orderingService.dto.response.FileQR
import java.net.URI

interface QrGeneratorService {
    fun generateFileQR(
        uri: URI,
        size: Int,
    ): FileQR?
}
