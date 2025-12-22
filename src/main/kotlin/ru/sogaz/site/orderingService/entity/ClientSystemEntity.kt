package ru.sogaz.site.orderingService.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "client_systems")
class ClientSystemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    val id: UUID? = null,
    @Column(name = "external_system_code", unique = true, length = 50, nullable = false)
    val externalSystemCode: String,
    @Column(name = "external_system_name", nullable = false)
    val externalSystemName: String,
    @Column(name = "permission_return", nullable = false)
    val permissionReturn: Boolean = false,
    @Column(name = "skip_sending_errors_queue", nullable = false)
    val skipSendingErrorsQueue: Boolean = false,
    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    val createDate: Instant,
    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    val updateDate: Instant,
)
