package ru.sogaz.site.orderingService.mappers.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.payment.client.model.ContractInfo
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Mapper(componentModel = "spring")
interface ContractInfoMapper {
    companion object {
        @JvmStatic
        @Named("instantToOffsetDateTime")
        fun instantToOffsetDateTime(instant: Instant): OffsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())
    }

    @Mapping(target = "contractDate", source = "contractDate", qualifiedByName = ["instantToOffsetDateTime"])
    fun subOrderToContractInfo(subOrder: SubOrderEntity): ContractInfo
}
