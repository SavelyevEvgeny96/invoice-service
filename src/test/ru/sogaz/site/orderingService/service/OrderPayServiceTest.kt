package ru.sogaz.site.orderingService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_ALREADY_PAID
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CLOSED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_NOT_FOUND
import ru.sogaz.site.orderingService.dao.ClientSystemDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.request.PayQueryParams
import ru.sogaz.site.orderingService.dto.response.PaymentPage
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.mappers.OrderManualMapper
import ru.sogaz.site.orderingService.service.impl.OrderServiceImpl
import ru.sogaz.site.orderingService.service.payment.PaymentService
import ru.sogaz.site.orderingService.service.shortLinks.ShortLinksIntegration
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OrderPayServiceTest {
    companion object {
        private val validUUID = UUID.randomUUID()
        private val invalidUUID = UUID.randomUUID()
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var subOrderDao: SubOrderDao

    @MockK
    private lateinit var paymentService: PaymentService

    @MockK
    private lateinit var clientSystemDao: ClientSystemDao

    @RelaxedMockK
    private lateinit var orderManualMapper: OrderManualMapper

    private lateinit var orderService: OrderServiceImpl

    @RelaxedMockK
    private lateinit var payQueryParams: PayQueryParams

    @RelaxedMockK
    private lateinit var testOrder: OrderEntity

    @MockK
    private lateinit var shortLinksIntegration: ShortLinksIntegration

    @RelaxedMockK
    private lateinit var paymentPage: PaymentPage
    private val hostNameApp = "https://pay.test2/"
    private val payBasePath = "https://pay.test"
    private val paymentUrlSuffix = "/payment/p/"

    @BeforeEach
    fun beforeEach() {
        orderService =
            OrderServiceImpl(
                orderDao = orderDao,
                paymentService = paymentService,
                clientSystemDao = clientSystemDao,
                payBasePath = payBasePath,
                subOrderDao = subOrderDao,
                orderManualMapper = orderManualMapper,
                hostNameApp = hostNameApp,
                paymentUrlSuffix = paymentUrlSuffix,
                shortLinksIntegration = shortLinksIntegration,
            )
        every { orderDao.findById(validUUID) } returns testOrder
        every { paymentService.payCard(testOrder, payQueryParams) } returns paymentPage
    }

    @Test
    fun `should throw a businessException when order is not found`() {
        every { orderDao.findById(invalidUUID) } returns null

        val ex = assertThrows<BusinessException> { orderService.payCard(invalidUUID, payQueryParams) }

        Assertions
            .assertThat(ex)
            .returns(ERROR_CODE_ORDER_NOT_FOUND, BusinessException::getErrorCode)
    }

    @Test
    fun `should throw a businessException when order is already paid`() {
        every { testOrder.status } returns OrderStatusesEnum.SUCCESS

        val ex = assertThrows<BusinessException> { orderService.payCard(validUUID, payQueryParams) }

        Assertions
            .assertThat(ex)
            .returns(ERROR_CODE_ORDER_ALREADY_PAID, BusinessException::getErrorCode)
    }

    @Test
    fun `should throw a businessException when order is closed`() {
        every { testOrder.status } returns OrderStatusesEnum.OVERDUE

        val ex = assertThrows<BusinessException> { orderService.payCard(validUUID, payQueryParams) }

        Assertions
            .assertThat(ex)
            .returns(ERROR_CODE_ORDER_CLOSED, BusinessException::getErrorCode)
    }

    @Test
    fun `should correctly return paymentPage`() {
        every { testOrder.status } returns OrderStatusesEnum.NEW

        val paymentPage = orderService.payCard(validUUID, payQueryParams)

        Assertions
            .assertThat(paymentPage)
            .isEqualTo(this.paymentPage)
    }
}
