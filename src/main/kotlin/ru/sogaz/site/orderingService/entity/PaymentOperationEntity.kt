package ru.sogaz.site.orderingService.entity

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
import ru.sogaz.site.orderingService.enums.BankEnum
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payment_operations")
class PaymentOperationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var state: String,
    @Enumerated(EnumType.STRING)
    var bank: BankEnum,
    var type: String,
    var depersonalization: Boolean = false,
    var payDate: Instant,
    @UpdateTimestamp
    var updateDate: Instant?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    var orderEntity: OrderEntity?,
)
