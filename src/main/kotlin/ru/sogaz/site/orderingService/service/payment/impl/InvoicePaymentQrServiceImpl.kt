package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_ALREADY_PAID
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_CLOSED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomOrderingServiceErrors.Companion.ERROR_CODE_ORDER_NOT_FOUND
import ru.sogaz.site.orderingService.dao.CompanyDetailsQrDao
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.data.PaymentQrData
import ru.sogaz.site.orderingService.dto.response.InvoicePaymentQr
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.mappers.payment.PaymentQrMapper
import ru.sogaz.site.orderingService.service.payment.InvoicePaymentQrService
import ru.sogaz.site.orderingService.service.payment.QrGeneratorService
import java.util.UUID

@Service
@Transactional(readOnly = true)
class InvoicePaymentQrServiceImpl(
    private val orderDao: OrderDao,
    private val companyDetailsQrDao: CompanyDetailsQrDao,
    private val paymentQrMapper: PaymentQrMapper,
    private val qrGeneratorService: QrGeneratorService,
) : InvoicePaymentQrService {
    override fun generate(
        invoiceId: UUID,
        bank: String,
    ): InvoicePaymentQr {
        val order = orderDao.findById(invoiceId) ?: throw BusinessException(ERROR_CODE_ORDER_NOT_FOUND)
        order.checkStatus()
        val company = checkNotNull(companyDetailsQrDao.findByBank(bank)) { "Не найдены реквизиты компании для банка $bank" }
        val qrData = paymentQrMapper.toPaymentQrData(order, order.subOrders.firstOrNull(), company)
        val qr = checkNotNull(qrGeneratorService.generateFileQR(qrData.toGostString())) { "Не удалось сформировать QR" }
        return InvoicePaymentQr(qr.content, qr.mediaType)
    }

    private fun OrderEntity.checkStatus() {
        when {
            status.isPaidFor() -> throw BusinessException(ERROR_CODE_ORDER_ALREADY_PAID)
            !status.isAvailable() -> throw BusinessException(ERROR_CODE_ORDER_CLOSED)
        }
    }

    private fun PaymentQrData.toGostString(): String =
        "ST00012|Name=$name|PersonalAcc=$personalAcc|BankName=$bankName|BIC=$bic|CorrespAcc=$correspAcc" +
            "|Sum=$sum|PayeeINN=$payeeInn|LastName=$lastName|FirstName=$firstName|MiddleName=$middleName" +
            "|Purpose=СТРАХОВОЙ ВЗНОС ПО ДОГОВОРУ СТРАХОВАНИЯ $contractNumber от $contractDate " +
            "СТРАХОВАТЕЛЬ ${listOf(lastName, firstName, middleName).filter(String::isNotBlank).joinToString(" ")}. НДС НЕ ОБЛАГАЕТСЯ. QR"
}
