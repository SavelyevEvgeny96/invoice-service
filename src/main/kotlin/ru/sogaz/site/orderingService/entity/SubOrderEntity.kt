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
    var id: UUID?,
    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "subscription_id", nullable = false)
    var orderEntity: OrderEntity?,
    @Column(name = "policy_id", nullable = false)
    var policyId: String,
    @Column(name = "policy_number", nullable = false)
    var policyNumber: String = "",
    @Column(name = "contract_id")
    var contractId: String?,
    @Column(name = "contract_number")
    var contractNumber: String?,
    @Column(name = "doc_type")
    var docType: String?,
    @Column(name = "channel")
    var channel: String?,
    @Column(name = "main_contract_check")
    var mainContractCheck: Boolean,
    @Column(name = "insurance_program")
    var insuranceProgram: String?,
    @Column(name = "type_insurance")
    var typeInsurance: String?,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal?,
    @Column(name = "manager_email")
    var managerEmail: String?,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: Instant?,
    @Column(name = "contract_date")
    var contractDate: Instant?,
    @Column(name = "policy_date")
    var policyDate: Instant?,
)
