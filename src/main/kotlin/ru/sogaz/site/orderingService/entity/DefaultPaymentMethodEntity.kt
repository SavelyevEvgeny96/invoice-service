package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "default_payment_methods")
class DefaultPaymentMethodEntity(
    @Id
    var id: UUID? = null,
    @Column(name = "name")
    var name: String? = null,
    @Column(name = "availability")
    var availability: Boolean = false,
    @Column(name = "create_date")
    var createDate: Instant? = null,
    @Column(name = "update_date")
    var updateDate: Instant? = null,
)
