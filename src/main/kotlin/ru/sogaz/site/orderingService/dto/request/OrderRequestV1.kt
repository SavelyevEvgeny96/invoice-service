package ru.sogaz.site.orderingService.dto.request

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.validation.constraint.Phone
import ru.sogaz.site.orderingService.validation.constraint.RussianNameValid
import ru.sogaz.site.orderingService.validation.constraint.SameChannelInOrders
import ru.sogaz.site.orderingService.validation.constraint.SogazDomain
import ru.sogaz.site.orderingService.validation.constraint.UniqueMainContract
import java.time.Instant
import java.util.UUID

/**
 * @property orders Лист данных по отдельному Order(каждый payments имеет отдельную запись в таблице subOrder)
 * @property orderEndDate Дата и время окончания действия ссылки на оплату (обязательное поле)
 * @property recipientEmail Электронная почта получателя (обязательное поле)
 * @property bank Банк для совершения операции (необязательное поле, если не указан — используется дефолтный банк из конфигурации)
 * @property urlToReturn URL для перехода после успешной оплаты (обязательное поле)
 * @property urlToDecline URL для перехода после неуспешной оплаты (обязательное поле)

 */
data class OrderRequestV1(
    @get:Valid
    @field:UniqueMainContract(message = "{validation.orderRequest.uniqueMainContract}")
    @field:SameChannelInOrders(message = "{validation.orderRequest.sameChannelInOrders}")
    var orders: MutableList<SubOrderRequestV1> = mutableListOf(),
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    @field:Future(message = "{validation.orderRequest.date.future}")
    var orderEndDate: Instant? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    var recipientEmail: String = "",
    var recipientUserId: String? = null,
    var unifiedId: String? = null,
    @field:Phone(message = "{validation.ordersUserRequest.phone.invalid}")
    var recipientPhone: String? = null,
    @param:Parameter(name = "urlToReturnS", description = "URL для перехода после успешной оплаты")
    @field:SogazDomain
    var urlToReturn: String? = null,
    @param:Parameter(name = "urlToDecline", description = "URL для перехода после неуспешной оплаты ")
    @field:SogazDomain
    var urlToDecline: String? = null,
    @field:NotNull(message = "{validation.orderRequest.notBlank}")
    var saveCard: Boolean? = null,
    var subscriptionId: String = "",
    var clientId: String? = null,
    var channelSale: String? = null,
    @field:RussianNameValid
    var policyholder: String? = null,
    var orderIdRecurrent: UUID? = null,
    var bank: BankEnum? = null,
    var keyCard: String? = null,
)
