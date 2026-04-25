package ru.sogaz.site.orderingService.shedulers

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dto.data.OverdueOrdersEvent
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.repository.OrderRepository
import java.time.Clock
import java.time.Instant

@Component
class OverdueOrdersJob(
    private val orderRepository: OrderRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val clock: Clock,
) {
    @Scheduled(cron = "\${crons.overdue.cron}")
    @SchedulerLock(name = "overdueOrdersJob")
    @Transactional
    fun run() {
        val now: Instant = Instant.now(clock)

        val orders =
            orderRepository.findOverdueOrders(
                states = listOf(OrderStatusesEnum.NEW, OrderStatusesEnum.UPDATE),
                now = now,
            )

        if (orders.isEmpty()) return

        orders.forEach { order ->
            order.status = OrderStatusesEnum.OVERDUE
            order.updateDate = now
        }

        eventPublisher.publishEvent(OverdueOrdersEvent(orders))
    }
}
