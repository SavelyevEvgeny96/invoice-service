package ru.sogaz.site.orderingService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Репозиторий информации о платежном апи.
 */
@ConfigurationProperties(prefix = "app.payment")
data class PaymentApiProperties(
    val paymentHost: String,
    val paymentUrlSuffix: String,
    val paymentSbpUrlSuffix: String,
    val isSbpActive: Boolean,
    val isQrGeneratorActive: Boolean,
    val qrCodeSize: Integer,
    val hostNameApp: String,
    val pagepayinfoHost: String,
    val pagepayinfoUrlSuffix: String,
)
