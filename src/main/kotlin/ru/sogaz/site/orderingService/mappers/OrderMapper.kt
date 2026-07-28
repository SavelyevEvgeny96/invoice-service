package ru.sogaz.site.orderingService.mappers

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.data.MetaInfoOrder
import ru.sogaz.site.orderingService.dto.request.CreateOrderCommand
import ru.sogaz.site.orderingService.dto.request.CreateSubOrderCommand
import ru.sogaz.site.orderingService.dto.request.SubOrderDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.enums.PaymentQrBank
import ru.sogaz.site.orderingService.enums.TypeOperationRequestEnum
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = [Instant::class],
)
abstract class OrderMapper {
    private companion object {
        val NON_ALPHANUMERIC_REGEX = Regex("[^A-Za-zА-Яа-яЁё0-9]")
    }

    @Mapping(target = "paymentEndDate", source = "orderEndDate")
    @Mapping(target = "recipientEmail", expression = "java(nullToEmpty(dto.getRecipientEmail()))")
    @Mapping(target = "recipientPhone", expression = "java(nullToEmpty(dto.getRecipientPhone()))")
    @Mapping(target = "premiumAmount", expression = "java(mapPremium(dto.getSubOrders()))")
    @Mapping(target = "receiptState", constant = "NONE")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createDate", expression = "java( Instant.now() )")
    @Mapping(target = "clientId", expression = "java(mapClientId(dto.getMetaInfo()))")
    @Mapping(target = "queueStatusResultName", expression = "java(buildQueueStatusResultName(dto.getMetaInfo()))")
    @Mapping(target = "subOrders", ignore = true)
    abstract fun toOrderEntity(dto: OrderPayloadDto): OrderEntity

    @Mapping(target = "orderEntity", source = "order")
    @Mapping(target = "premiumAmount", source = "dto.premiumAmountDto")
    @Mapping(target = "createDate", expression = "java(Instant.now())")
    abstract fun toSubOrderEntity(
        dto: SubOrderDto,
        order: OrderEntity,
    ): SubOrderEntity

    @Mapping(
        target = "paymentEndDate",
        source = "orderEndDate",
    )
    @Mapping(
        target = "recurrent",
        expression = "java(command.getOrderIdRecurrent() != null)",
    )
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "receiptState", constant = "NONE")
    @Mapping(target = "recipientPhone", defaultValue = "")
    @Mapping(target = "queueStatusResultName", expression = "java(mapClientIdToQueueResultName(command))")
    @Mapping(target = "premiumAmount", expression = "java(calculatePremiumAmount(command.getSubOrders()))")
    @Mapping(target = "bank", expression = "java(mapBankBySubOrders(command.getSubOrders()))")
    @Mapping(target = "paymentMethodList", expression = "java(mapPaymentMethodList(command.getPaymentMethodList()))")
    @Mapping(target = "bankQr", expression = "java(mapBankQr(command.getBankQr()))")
    abstract fun fromCommand(command: CreateOrderCommand): OrderEntity

    @Named("mapPaymentMethodList")
    fun mapPaymentMethodList(paymentMethods: List<PaymentMethod>?): Set<PaymentMethod>? = paymentMethods?.toSet()

    @Named("mapBankQr")
    fun mapBankQr(banks: List<PaymentQrBank>?): Set<PaymentQrBank>? = banks?.toSet()

    /** Supports the legacy comma-separated/JSON-array representation used by older create commands. */
    fun mapBankQr(banks: String?): Set<PaymentQrBank>? =
        banks
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.removePrefix("[")
            ?.removeSuffix("]")
            ?.split(',')
            ?.mapNotNull { value ->
                value.trim().trim('"').uppercase().let { normalized ->
                    PaymentQrBank.entries.firstOrNull { it.name == normalized }
                }
            }?.toSet()

    @Named("mapBankBySubOrders")
    fun mapBankBySubOrders(subOrders: List<CreateSubOrderCommand>?): String? =
        when {
            subOrders
                .orEmpty()
                .any { it.typeOperation == TypeOperationRequestEnum.PAYMENT_SUBSCRIPTION.name } -> BankEnum.GPB.name

            else -> null
        }

    abstract fun fromCommand(command: CreateSubOrderCommand): SubOrderEntity

    @AfterMapping
    protected fun pinOrderToSubOrders(
        @MappingTarget order: OrderEntity,
    ): OrderEntity =
        order.apply {
            subOrders.forEach {
                it.orderEntity = this
            }
        }

    // ---------- Helpers ----------
    @Named("nullToEmpty")
    fun nullToEmpty(value: String?): String = value ?: ""

    @Named("mapClientId")
    fun mapClientId(metaInfo: List<MetaInfoOrder>): String? = metaInfo.firstOrNull()?.author

    @Named("mapPremium")
    fun mapPremium(subOrders: List<SubOrderDto>?): BigDecimal? =
        subOrders
            ?.map { it.premiumAmountDto }
            ?.fold(BigDecimal.ZERO, BigDecimal::add)
            ?.takeIf { it > BigDecimal.ZERO }

    @Named("calculatePremiumAmount")
    fun calculatePremiumAmount(subOrders: List<CreateSubOrderCommand>): BigDecimal =
        subOrders
            .sumOf { it.premiumAmount }
            .setScale(2, RoundingMode.HALF_UP)

    @Named("mapQueueResultName")
    protected fun buildQueueStatusResultName(metaInfo: List<MetaInfoOrder>): String? =
        metaInfo
            .firstOrNull()
            ?.author
            ?.takeIf { it.isNotBlank() }
            ?.replace(NON_ALPHANUMERIC_REGEX, ".")
            ?.let { "payment.status.$it.created" }

    @Named("mapClientIdToQueueResultName")
    protected fun mapClientIdToQueueResultName(command: CreateOrderCommand): String? =
        command.clientId
            ?.takeIf { it.isNotBlank() }
            ?.replace(NON_ALPHANUMERIC_REGEX, ".")
            ?.let { "payment.status.$it.created" }
}
