

package ru.sogaz.site.orderingService.dto.request

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.orderingService.enums.TypeInsuranceEnum
import ru.sogaz.site.orderingService.enums.TypeOperationRequestEnum
import ru.sogaz.site.orderingService.validation.constraint.StrictPremiumBigDecimalDeserializer
import ru.sogaz.site.orderingService.validation.constraint.ValidEnum
import ru.sogaz.site.orderingService.validation.constraint.ValidPremiumAmount
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO для запроса на создание подзаказа v2.
 */

data class SubOrderRequestV2(
    @field:JsonSetter(nulls = Nulls.FAIL)
    @field:JsonDeserialize(using = StrictPremiumBigDecimalDeserializer::class)
    @field:ValidPremiumAmount
    val premium: BigDecimal = BigDecimal.ZERO,
    val policyId: String = "",
    val policyNumber: String = "",
    val policyDate: Instant? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val agreementNumber: String? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    val agreementId: String? = null,
    val agreementDate: Instant? = null,
    @field:NotNull(message = "{validation.orderRequest.notBlank}")
    @field:ValidEnum(
        enumClass = TypeInsuranceEnum::class,
        message = "{validation.orderRequest.insuranceKind}",
    )
    val insuranceKind: String? = null,
    val program: String? = null,
    val channel: String? = null,
    @field:ValidEnum(
        enumClass = TypeOperationRequestEnum::class,
        message = "{validation.orderRequest.typeOperation}",
    )
    var typeOperation: String = "",
    @field:Email(message = "{validation.orderRequest.recipientEmail.email}")
    val managerEmail: String = "",
)
