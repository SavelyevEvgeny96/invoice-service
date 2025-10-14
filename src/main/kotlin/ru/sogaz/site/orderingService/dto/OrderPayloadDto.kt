package ru.sogaz.site.orderingService.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.validation.constraint.EmailValid
import java.time.Instant

data class OrderPayloadDto(
    @field:NotNull
    val metaInfo: List<MetaInfoOrder>,
    @field:NotBlank
    val orderId: String,
    @field:EmailValid
    val recipientEmail: String?, // email страхователя
    val recipientPhone: String?, // телефон страхователя
    val recipientUserId: String?, // ID личного кабинета
    val recipientGdId: String?, // золотой ID
    val recurrent: Boolean = false, // рекуррентный платёж
    val keyCard: String?, // ключ карты (если recurrent=true)
    val saveCard: Boolean? = null, // сохранять карту?
    val managerEmail: String?, // менеджер по заказу
    val orderEndDate: Instant?, // срок актуальности заказа
    val subOrders: List<SubOrderDto>, // список полисов внутри заказа
)
