package ru.sogaz.site.orderingService.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.ReceiptState
import java.math.BigDecimal
import java.math.RoundingMode
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
    @Column(name = "url_to_return")
    var urlToReturn: String?,
    @Column(name = "url_to_decline")
    var urlToDecline: String?,
    @Column(name = "save_card")
    var saveCard: Boolean?,
    @Column(name = "reg_card")
    var regCard: Boolean = false,
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "order_statuses_enum")
    var status: OrderStatusesEnum = OrderStatusesEnum.NEW,
    @Column(name = "recurrent")
    var recurrent: Boolean?,
    @Column(name = "payment_end_date")
    var paymentEndDate: Instant?,
    @Column(name = "refund_date")
    var refundDate: Instant?,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal?,
    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String?,
    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String?,
    @Column(name = "recipient_user_id")
    var recipientUserId: String?,
    @Column(name = "queue_status_result_name")
    var queueStatusResultName: String?,
    @Column(name = "skip_sending_queue")
    var skipSendingQueue: Boolean? = false,
    @Column(name = "skip_sending_receipt")
    var skipSendingReceipt: Boolean? = false,
    @Column(name = "skip_sending_errors_queue")
    var skipSendingErrorsQueue: Boolean? = false,
    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_state")
    var receiptState: ReceiptState = ReceiptState.NONE,
    @Column(name = "depersonalization")
    var depersonalization: Boolean,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant?,
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: Instant?,
) {
    @OneToMany(cascade = [(CascadeType.PERSIST)], fetch = FetchType.LAZY, mappedBy = "orderEntity")
    val subOrders: MutableList<SubOrderEntity> = mutableListOf()

    fun calculatePremiumAmount(): BigDecimal =
        subOrders
            .sumOf { it.premiumAmount ?: BigDecimal.ZERO }
            .setScale(2, RoundingMode.HALF_UP)
}
