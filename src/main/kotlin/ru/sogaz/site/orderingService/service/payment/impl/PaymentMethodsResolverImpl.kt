package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.PaymentMethod
import ru.sogaz.site.orderingService.repository.DefaultPaymentMethodRepository
import ru.sogaz.site.orderingService.service.payment.PaymentMethodsResolver

@Service
class PaymentMethodsResolverImpl(
    private val defaultPaymentMethodRepository: DefaultPaymentMethodRepository,
) : PaymentMethodsResolver {
    override fun resolve(order: OrderEntity): Set<PaymentMethod> =
        order.paymentMethodList?.takeIf { it.isNotEmpty() }
            ?: defaultPaymentMethodRepository.findAllByAvailabilityTrue()
                .mapNotNull { method -> method.name?.trim()?.uppercase()?.let(::parse) }
                .toSet()

    private fun parse(value: String): PaymentMethod? = runCatching { PaymentMethod.valueOf(value) }.getOrNull()
}
