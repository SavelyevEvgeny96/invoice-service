package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Named
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Mapper
abstract class PaymentPurposeMapper {
    companion object {
        private const val PAY_ONE_CONTRACT_INFO = "Оплата по договору %s от %s. Платежный сервис, дата операции %s"
        private const val CONTRACT_INFO = "%s от %s"
        private const val PARAM = "param"
        private const val EMPTY_PAY_INFO = "Платежный сервис, дата операции %s"
        private const val EMPTY_SUB_ORDERS = "В заказе отсутствуют контракты"

        private val DEFAULT_ZONE: ZoneId = ZoneId.systemDefault()
        private val DDMMYYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    @Named("mapRequestContractDescription")
    protected fun mapRequestContractDescription(subOrders: List<SubOrderEntity>): String {
        val operationDate = LocalDate.now(DEFAULT_ZONE).toContractDateFormat()
        return runCatching { mapRequestContractDescription(operationDate, subOrders) }
            .getOrElse { EMPTY_PAY_INFO.format(operationDate) }
    }

    private fun mapRequestContractDescription(
        operationDate: String,
        subOrders: List<SubOrderEntity>,
    ) = subOrders
        .findMainContract()
        .makeDescriptionForOneContract(operationDate)

    private fun List<SubOrderEntity>.findMainContract(): SubOrderEntity =
        when (size) {
            0 -> throw InnerException(getTraceId(), EMPTY_SUB_ORDERS)
            else -> findLast(SubOrderEntity::mainContractCheck) ?: first()
        }

    private fun SubOrderEntity.makeDescriptionForOneContract(opDate: String): String =
        PAY_ONE_CONTRACT_INFO.format(
            contractNumber,
            contractDate?.toContractDateFormat(),
            opDate,
        )

    private fun Instant.toContractDateFormat(): String =
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

    private fun SubOrderEntity.toParamValue(): String = CONTRACT_INFO.format(contractNumber, contractDate?.toContractDateFormat())
}
