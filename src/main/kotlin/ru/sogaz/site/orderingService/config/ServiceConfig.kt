package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.site.orderingService.service.impl.OrdersUserServiceImpl

@Configuration
class ServiceConfig {
    @Bean
    fun orderServiceConfig(
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
    ): OrdersUserService = OrdersUserServiceImpl(orderDao = orderDao, subOrderDao = subOrderDao)
}
