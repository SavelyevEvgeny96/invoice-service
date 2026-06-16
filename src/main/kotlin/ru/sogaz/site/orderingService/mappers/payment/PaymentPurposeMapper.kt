package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Named
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.payment.client.model.RedirectParams
import ru.sogaz.site.payment.client.model.StraightRedirectSchema
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Mapper
abstract class PaymentPurposeMapper {
    companion object {
        private const val PAY_CARD_ONE_CONTRACT_INFO = "Оплата по договору %s%s. Платежный сервис, дата операции %s"
        private const val PAY_SBP_ONE_CONTRACT_INFO = "Оплата по договору страхования %s"
        private const val CONTRACT_INFO = "%s%s"
        private const val PARAM = "param"
        private const val EMPTY_PAY_INFO = "Платежный сервис, дата операции %s"
        private const val EMPTY_SBP_PAY_INFO = "Оплата по договору страхования"
        private const val EMPTY_SUB_ORDERS = "В заказе отсутствуют контракты"

        private val DEFAULT_ZONE: ZoneId = ZoneId.systemDefault()
        private val DDMMYYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    // ===================== REDIRECT LOGIC =====================

    fun mapRedirectParams(
        params: PayQueryParams,
        order: OrderEntity,
    ): RedirectParams =
        RedirectParams().apply {
            urlToReturn = params.urlToReturn.takeIf { !it.isNullOrBlank() }
                ?: order.urlToReturn

            urlToReturnS = params.urlToReturnS.takeIf { !it.isNullOrBlank() }
                ?: order.urlToReturn

            urlToReturnF = params.urlToReturnF.takeIf { !it.isNullOrBlank() }
                ?: order.urlToDecline
        }

    fun mapSbpRedirectParams(
        params: PayQueryParams,
        order: OrderEntity,
    ): StraightRedirectSchema =
        StraightRedirectSchema().apply {
            urlToReturn =
                params.urlToReturn.takeIf { !it.isNullOrBlank() }
                    ?: order.urlToReturn
        }
    // ===================== EXISTING LOGIC =====================

    @Named("mapCardRequestContractDescription")
    protected fun mapCardRequestContractDescription(subOrders: List<SubOrderEntity>): String {
        val operationDate = LocalDate.now(DEFAULT_ZONE).toContractDateFormat()
        return runCatching { mapCardRequestContractDescription(operationDate, subOrders) }
            .getOrElse { EMPTY_PAY_INFO.format(operationDate) }
    }

    private fun mapCardRequestContractDescription(
        operationDate: String,
        subOrders: List<SubOrderEntity>,
    ) = subOrders
        .findMainContract()
        .makeCardPayDescriptionForOneContract(operationDate)

    private fun SubOrderEntity.makeCardPayDescriptionForOneContract(opDate: String): String =
        PAY_CARD_ONE_CONTRACT_INFO.format(
            contractNumber,
            contractDate?.toContractDateFormat() ?: "",
            opDate,
        )

    @Named("mapSbpRequestContractDescription")
    protected fun mapSbpRequestContractDescription(subOrders: List<SubOrderEntity>): String =
        runCatching { subOrders.findMainContract().makeSbpPayDescriptionForOneContract() }
            .getOrElse { EMPTY_SBP_PAY_INFO }

    private fun SubOrderEntity.makeSbpPayDescriptionForOneContract(): String = PAY_SBP_ONE_CONTRACT_INFO.format(contractNumber)

    private fun List<SubOrderEntity>.findMainContract(): SubOrderEntity =
        when (size) {
            0 -> throw InnerException(getTraceId(), EMPTY_SUB_ORDERS)
            else -> first()
        }

    private fun Instant.toContractDateFormat(): String =
        " от " +
            atZone(DEFAULT_ZONE)
                .toLocalDate()
                .toContractDateFormat()

    private fun LocalDate.toContractDateFormat(): String = format(DDMMYYYY)

    @Named("mapRequestParams")
    protected fun mapRequestParams(subOrders: List<SubOrderEntity>): Map<String, String> =
        subOrders
            .mapIndexed(::mapToParam)
            .toMap()

    private fun mapToParam(
        idx: Int,
        subOrder: SubOrderEntity,
    ): Pair<String, String> = "${PARAM}${idx + 1}" to subOrder.toParamValue()

    private fun SubOrderEntity.toParamValue(): String = CONTRACT_INFO.format(contractNumber, contractDate?.toContractDateFormat() ?: "")
}
