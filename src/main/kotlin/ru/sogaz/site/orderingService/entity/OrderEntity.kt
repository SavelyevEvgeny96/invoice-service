package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @Column(name = "order_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    var orderId: UUID?,
    @Column(name = "unified_id")
    var unifiedId: String?,
    @Column(name = "client_id")
    var clientId: String? = null,
    @Column(name = "bank")
    var bank: String?,
    @Column(name = "policyholder")
    var policyholder: String?,
    @Column(name = "payment_type")
    var paymentType: String?,
    @Column(name = "subscription_id")
    var subscriptionId: String?,
    @Column(name = "key_card")
    var keyCard: String?,
    @Column(name = "save_card")
    var saveCard: Boolean?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderStatusesEnum? = OrderStatusesEnum.NEW,
    @Column(name = "recurrent")
    var recurrent: Boolean?,
    @Column(name = "payment_end_date")
    var paymentEndDate: Instant?,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal?,
    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String?,
    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String?,
    @Column(name = "recipient_user_id")
    var recipientUserId: String?,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant?,
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: Instant?,
)
