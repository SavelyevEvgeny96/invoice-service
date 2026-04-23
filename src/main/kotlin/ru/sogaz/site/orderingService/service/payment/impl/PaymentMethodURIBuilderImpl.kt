package ru.sogaz.site.orderingService.service.payment.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.properties.PaymentApiProperties
import ru.sogaz.site.orderingService.service.payment.PaymentMethodURIBuilder
import java.net.URI
import java.util.UUID

@Service
class PaymentMethodURIBuilderImpl(
    private val paymentApiProperties: PaymentApiProperties,
) : PaymentMethodURIBuilder {
    private val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun buildPayCardURI(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "${paymentApiProperties.paymentHostPagePayInfo}${paymentApiProperties.paymentUrlSuffix}$orderId",
            payQueryParams.toQueryParams(),
        )

    override fun buildPaySbpURI(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "${paymentApiProperties.paymentHost}${paymentApiProperties.paymentSbpUrlSuffix}$orderId",
            payQueryParams.toQueryParams(),
        )

    private fun buildUri(
        uriString: String,
        params: MultiValueMap<String, String>,
    ): URI =
        UriComponentsBuilder
            .fromUriString(uriString)
            .queryParams(params)
            .toUriString()
            .run(URI::create)

    private fun PayQueryParams.toQueryParams(): MultiValueMap<String, String> = MultiValueMap.fromSingleValue(toMap())

    private fun PayQueryParams.toMap(): Map<String, String> = objectMapper.convertValue(this)
}
