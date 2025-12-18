package ru.sogaz.site.orderingService.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.validation.constraint.SameChannelInOrders
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
data class OrderRequest(
    @get:Valid
    @field:UniqueMainContract(message = "{validation.orderRequest.uniqueMainContract}")
    @field:SameChannelInOrders(message = "{validation.orderRequest.sameChannelInOrders}")
    var orders: MutableList<SubOrderRequest> = mutableListOf(),
    @field:NotNull(message = "{validation.orderRequest.date.notNull}")
    @field:Future(message = "{validation.orderRequest.date.future}")
    var orderEndDate: Instant? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    var recipientEmail: String = "",
    var recipientUserId: String? = null,
    var unifiedId: String? = null,
    var recipientPhone: String? = null,
    var urlToReturn: String? = null,
    var urlToDecline: String? = null,
    var saveCard: Boolean = false,
    var subscriptionId: String = "",
    var clientId: String? = null,
    var policyholder: String? = null,
    var orderIdRecurrent: UUID? = null,
    var bank: BankEnum? = null,
    var keyCard: String? = null,
)
