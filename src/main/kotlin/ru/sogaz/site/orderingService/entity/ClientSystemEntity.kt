package ru.sogaz.site.orderingService.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "client_systems")
class ClientSystemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    val id: UUID?,
    @Column(name = "external_system_code", unique = true, length = 50)
    val externalSystemCode: String,
    @Column(name = "external_system_name", nullable = false)
    val externalSystemName: String,
)
