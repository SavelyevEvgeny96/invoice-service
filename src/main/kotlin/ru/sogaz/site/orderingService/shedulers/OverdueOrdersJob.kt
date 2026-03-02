package ru.sogaz.site.orderingService.shedulers

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.repository.OrderRepository
import java.time.Clock
import java.time.Instant

@Component
class OverdueOrdersJob(
    private val orderRepository: OrderRepository,
    private val clock: Clock,
) {
    @Scheduled(cron = "\${crons.overdue.cron}")
    @SchedulerLock(name = "overdueOrdersJob")
    @Transactional
    fun run() {
        val now: Instant = Instant.now(clock)
        orderRepository.markOverdue(
            listOf(OrderStatusesEnum.NEW, OrderStatusesEnum.UPDATE),
            OrderStatusesEnum.OVERDUE,
            now,
        )
    }
}
