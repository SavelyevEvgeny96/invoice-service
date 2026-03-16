package ru.sogaz.site.orderingService.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.validation.constraint.EmailValid
import ru.sogaz.site.orderingService.validation.constraint.UniqueMainContract
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrderPayloadDto(
    @field:NotNull
    val metaInfo: List<MetaInfoOrder>,
    @field:NotBlank
    val subscriptionId: String, // id подписки
    @field:EmailValid
    val recipientEmail: String?, // email страхователя
    @field:NotBlank
    val bank: String,
    @field:NotBlank
    val paymentType: String?,
    val recipientPhone: String?, // телефон страхователя
    val recipientUserId: String?, // ID личного кабинета
    val keyCard: String?, // ключ карты (если recurrent=true)
    val orderEndDate: Instant?, // срок актуальности заказа
    val unifiedId: String? = null, // золотой ID
    val policyholder: String? = null,
    @field:UniqueMainContract
    val subOrders: List<SubOrderDto>, // список полисов внутри заказа
    @JsonProperty("order_id_recurrent")
    val orderIdRecurrent: UUID? = null, // новое необязательное поле
)
