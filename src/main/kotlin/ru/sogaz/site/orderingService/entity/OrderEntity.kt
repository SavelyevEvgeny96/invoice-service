package ru.sogaz.site.orderingService.entity

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
import ru.sogaz.site.orderingService.enums.ApiVersionEnum
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.ReceiptState
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @Column(name = "order_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    var orderId: UUID? = null,
    @Column(name = "unified_id")
    var unifiedId: String? = null,
    @Column(name = "client_id")
    var clientId: String? = null,
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
    @Column(name = "type_payment_operation")
    var typePaymentOperation: String? = null,
    @Column(name = "url_to_return")
    var urlToReturn: String? = null,
    @Column(name = "url_to_decline")
    var urlToDecline: String? = null,
    @Column(name = "save_card")
    var saveCard: Boolean? = null,
    @Column(name = "url_pay_page_short")
    var urlPayPageShort: String? = null,
    @Column(name = "reg_card")
    var regCard: Boolean = false,
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "order_statuses_enum")
    var status: OrderStatusesEnum = OrderStatusesEnum.NEW,
    @Column(name = "recurrent")
    var recurrent: Boolean? = null,
    @Column(name = "payment_end_date")
    var paymentEndDate: Instant? = null,
    @Column(name = "refund_date")
    var refundDate: Instant? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "version_api")
    var versionApi: ApiVersionEnum? = null,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,
    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String? = null,
    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String? = null,
    @Column(name = "recipient_user_id")
    var recipientUserId: String? = null,
    @Column(name = "queue_status_result_name")
    var queueStatusResultName: String,
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
    var depersonalization: Boolean? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: Instant? = null,
) {
    fun addSubOrder(subOrder: SubOrderEntity) {
        subOrders.add(subOrder)
        subOrder.orderEntity = this
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderEntity")
    val subOrders: MutableList<SubOrderEntity> = mutableListOf()
}
