package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "receipts")
data class ReceiptEntity(
    @Id
    var id: UUID,
    var paymentId: UUID,
    var orderId: UUID?,
    var state: String,
    var amount: BigDecimal,
    var typeOperation: String,
    var link: String?,
    var errorText: String?,
    var sendingTime: Instant?,
    @CreationTimestamp
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
