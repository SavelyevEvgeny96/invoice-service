package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "company_details_qr")
class CompanyDetailsQrEntity(
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    var id: UUID,
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "personal_acc", nullable = false)
    var personalAcc: String,
    @Column(name = "bank_name", nullable = false)
    var bankName: String,
    @Column(name = "bic", nullable = false)
    var bic: String,
    @Column(name = "corres_acc", nullable = false)
    var correspAcc: String,
    @Column(name = "payee_inn", nullable = false)
    var payeeInn: String,
    @Column(name = "kpp", nullable = false)
    var kpp: String,
    @Column(name = "bank", nullable = false, unique = true)
    var bank: String,
    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    var createDate: Instant? = null,
    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    var updateDate: Instant? = null,
)
