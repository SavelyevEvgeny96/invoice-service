package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sub_orders")
class SubOrderEntity(
    @Id
    @Column(name = "sub_order_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false)
    var orderEntity: OrderEntity?,
    @Column(name = "policy_id", nullable = false)
    var policyId: String? = null,
    @Column(name = "policy_number", nullable = false)
    var policyNumber: String = "",
    @Column(name = "contract_id")
    var contractId: String? = null,
    @Column(name = "contract_number")
    var contractNumber: String? = null,
    @Column(name = "doc_type")
    var docType: String? = null,
    @Column(name = "channel")
    var channel: String? = null,
    @Column(name = "main_contract_check")
    var mainContractCheck: Boolean? = null,
    @Column(name = "send_status_product")
    var sendStatusProduct: Boolean? = false,
    @Column(name = "insurance_program")
    var insuranceProgram: String? = null,
    @Column(name = "type_insurance")
    var typeInsurance: String? = null,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,
    @Column(name = "manager_email")
    var managerEmail: String? = null,
    @Column(name = "type_operation")
    var typeOperation: String? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: Instant? = null,
    @Column(name = "contract_date")
    var contractDate: Instant? = null,
    @Column(name = "policy_date")
    var policyDate: Instant? = null,
)
