package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sub_orders")
data class SubOrderEntity(
    @Id
    @Column(name = "sub_order_id", columnDefinition = "uuid")
    var id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "subscription_id", nullable = false)
    var orderEntity: OrderEntity? = null,
    @Column(name = "policy_id", nullable = false)
    var policyId: String = "",
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
    var mainContractCheck: Boolean = false,
    @Column(name = "insurance_program")
    var insuranceProgram: String? = null,
    @Column(name = "type_insurance")
    var typeInsurance: String? = null,
    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,
    @Column(name = "manager_email")
    var managerEmail: String? = null,
    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,
    @Column(name = "update_date")
    var updateDate: Instant? = null,
    @Column(name = "contract_date")
    var contractDate: Instant? = null,
    @Column(name = "policy_date")
    var policyDate: Instant? = null,
)
