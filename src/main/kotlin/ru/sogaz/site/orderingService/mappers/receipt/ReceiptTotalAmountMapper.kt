package ru.sogaz.site.orderingService.mappers.receipt

import org.mapstruct.Mapper
import org.mapstruct.Named
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import java.math.BigDecimal

@Mapper
abstract class ReceiptTotalAmountMapper {
    companion object {
        private const val ERROR_FRACTION_SUM = "Дробная часть должна содержать не более 2 знаков"
        private const val ERROR_HOLL_SUM = "Целая часть должна содержать не более 8 знаков"
        private const val ERROR_INCORRECT_SUM = "Некорректный формат суммы: "
    }

    @Named("mapToBigDecimalAmount")
    fun mapToBigDecimalAmount(amount: String): BigDecimal =
        try {
            amount
                .replace(" ", "")
                .replace(",", ".")
                .toBigDecimal()
                .also(::checkAmount)
        } catch (ignore: NumberFormatException) {
            throw InnerException(getTraceId(), ERROR_INCORRECT_SUM + amount)
        }

    private fun checkAmount(amount: BigDecimal) {
        val parts = amount.toString().split(".")
        if (parts.size > 1 && parts[1].length > 2) {
            throw InnerException(getTraceId(), ERROR_FRACTION_SUM)
        }
        if (parts[0].length > 8) {
            throw InnerException(getTraceId(), ERROR_HOLL_SUM)
        }
    }
}
