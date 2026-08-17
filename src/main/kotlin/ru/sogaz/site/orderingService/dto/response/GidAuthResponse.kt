package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GidAuthResponse(
    val gidId: String?,
    val refreshToken: String?,
    val paymentMethods: List<List<PaymentMethodDto>>? = emptyList()
)

data class PaymentMethodDto(
    val id: UUID?,
    val type: String?,
    val supportsRecurring: Boolean?,
    val acquirer: AcquirerDto?,
    val provider: ProviderDto?,
    val paymentSystem: PaymentSystemDto?,
    val details: PaymentDetailsDto?
)

data class AcquirerDto(
    val slug: String?,
    val name: String?
)

data class ProviderDto(
    val slug: String?,
    val name: String?,
    val icon: String?
)

data class PaymentSystemDto(
    val slug: String?,
    val name: String?
)

data class PaymentDetailsDto(
    val description: String?,
    val lastDigits: String?
)