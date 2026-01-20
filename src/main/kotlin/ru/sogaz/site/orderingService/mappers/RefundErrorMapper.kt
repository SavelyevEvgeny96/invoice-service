package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Builder
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.dto.data.RefundErrorDto
import ru.sogaz.site.orderingService.dto.data.RefundPayloadDto
import ru.sogaz.site.orderingService.enums.RefundErrorReason

@Mapper(
    componentModel = "spring",
    builder = Builder(disableBuilder = true),
)
interface RefundErrorMapper {
    companion object {
        @JvmStatic
        @Named("mapErrorText")
        fun mapErrorText(reason: RefundErrorReason): String =
            when (reason) {
                RefundErrorReason.NO_ACCESS ->
                    "У системы отсутствуют права на выполнение операции возврата"

                RefundErrorReason.ORDER_NOT_FOUND ->
                    "Номер счета не найден"

                RefundErrorReason.NOT_PAID_FOR ->
                    "Счет не оплачен"
            }
    }

    @Mapping(target = "metaInfo", source = "payload.metaInfo")
    @Mapping(target = "orderId", source = "payload.orderId")
    @Mapping(target = "status", constant = "error")
    @Mapping(target = "errorText", source = "reason", qualifiedByName = ["mapErrorText"])
    fun toErrorDto(
        payload: RefundPayloadDto,
        reason: RefundErrorReason,
    ): RefundErrorDto
}
