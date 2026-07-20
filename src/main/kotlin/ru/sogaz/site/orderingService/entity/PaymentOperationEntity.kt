package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.orderingService.converter.MoscowInstantConverter
import ru.sogaz.site.orderingService.enums.BankEnum
import ru.sogaz.site.orderingService.enums.PaymentOperationStateEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payment_operations")
class PaymentOperationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var paymentId: UUID?,
    @Enumerated(EnumType.STRING)
    var state: PaymentOperationStateEnum,
    @Enumerated(EnumType.STRING)
    var bank: BankEnum,
    var operation: String,
    var type: String,
    var amount: BigDecimal,
    var depersonalization: Boolean = false,
    var paymentBankId: String?,
    var pan: String?,
    var paymentSystem: String?,
    @Convert(converter = MoscowInstantConverter::class)
    var payDate: Instant?,
    var payerIp: String?,
    var externalErrorCode: String?,
    var errorText: String?,
    @UpdateTimestamp
    var updateDate: Instant?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    var orderEntity: OrderEntity?,
)
