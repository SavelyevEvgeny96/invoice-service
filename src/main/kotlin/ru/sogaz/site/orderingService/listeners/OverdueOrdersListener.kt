package ru.sogaz.site.orderingService.listeners

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import ru.sogaz.site.orderingService.dto.data.OverdueOrdersEvent
import ru.sogaz.site.orderingService.service.rabbit.OverdueOrderPublisher

@Component
class OverdueOrdersListener(
    private val overdueOrderPublisher: OverdueOrderPublisher,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OverdueOrdersEvent) {
        overdueOrderPublisher.publish(event.orders)
    }
}
