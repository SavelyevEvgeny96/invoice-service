package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class TransactionConfig(
    private val dataSource: DataSource
) {
    @Bean
    fun dataSourceTransactionManager(): PlatformTransactionManager =
        DataSourceTransactionManager(dataSource)
}