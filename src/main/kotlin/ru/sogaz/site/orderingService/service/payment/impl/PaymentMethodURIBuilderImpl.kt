package ru.sogaz.site.orderingService.service.payment.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.service.payment.PaymentMethodURIBuilder
import java.net.URI
import java.util.UUID

@Service
class PaymentMethodURIBuilderImpl(
    @param:Value("\${api.payment.hostNameApp}")
    private val hostNameApp: String,
    @param:Value("\${api.payment.paymentUrlSuffix}")
    private val cardPayUriSuffix: String,
    @param:Value("\${api.payment.paymentSbpUrlSuffix}")
    private val sbpPayBaseUriSuffix: String,
) : PaymentMethodURIBuilder {
    private val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun buildPayCardURI(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "$hostNameApp$cardPayUriSuffix$orderId",
            payQueryParams.toQueryParams(),
        )

    override fun buildPaySbpURI(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "$hostNameApp$sbpPayBaseUriSuffix$orderId",
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
