package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @Column(name = "order_id", columnDefinition = "uuid")
    var orderId: UUID = UUID.randomUUID(),
    @Column(name = "unified_id")
    var unifiedId: String? = null,
    @Column(name = "url_to_return")
    var urlToReturn: String? = null,
    @Column(name = "url_to_decline")
    var urlToDecline: String? = null,
    @Column(name = "bank")
    var bank: String? = null,
    @Column(name = "policyholder")
    var policyholder: String? = null,
    @Column(name = "payment_type")
    var paymentType: String? = null,
    @Column(name = "subscription_id")
    var subscriptionId: String? = null,
    @Column(name = "key_card")
    var keyCard: String? = null,
    @Column(name = "save_card")
    var saveCard: Boolean? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderStatusesEnum = OrderStatusesEnum.NEW,
    @Column(name = "recurrent")
    var recurrent: Boolean? = null,
    @Column(name = "payment_end_date")
    var paymentEndDate: Instant? = null,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,
    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String = "",
    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String = "",
    @Column(name = "recipient_user_id")
    var recipientUserId: String? = null,
    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,
    @Column(name = "update_date")
    var updateDate: Instant? = null,
)
