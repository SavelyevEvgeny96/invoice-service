package ru.sogaz.site.orderingService.service.payment.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.response.FileQR
import ru.sogaz.site.orderingService.enums.MediaTypeValue
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.payment.QrGeneratorService
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest
import ru.sogaz.site.qr.generator.client.model.ResponseQRCodeData
import java.net.URI

@Service
class QrGeneratorServiceImpl(
    private val qrCodeControllerApi: QrCodeControllerApi,
) : QrGeneratorService {
    companion object {
        private const val QR_GENERATION_FAILED_LOG_MESSAGE = "При генерации QR кода возникла ошибка: {}"
    }

    private val logger = loggerFor(javaClass)

    override fun generateFileQR(text: String): FileQR? =
        QRCodeRequest()
            .apply { this.text = text }
            .run(::generateFileQR)

    override fun generateFileQR(
        uri: URI,
        size: Int,
    ): FileQR? =
        QRCodeRequest()
            .apply {
                text = uri.toString()
                this.size = size
            }.run(::generateFileQR)

    private fun generateFileQR(qrCodeRequest: QRCodeRequest): FileQR? =
        qrCodeRequest
            .runCatching(::requestQRFromQRGeneratorService)
            .onFailure(::logGenerationException)
            .getOrNull()

    private fun requestQRFromQRGeneratorService(qrCodeRequest: QRCodeRequest): FileQR =
        qrCodeRequest
            .run(qrCodeControllerApi::generateQRCode)
            .run(::makeFileQREntity)

    private fun makeFileQREntity(responseQRCodeData: ResponseQRCodeData) =
        FileQR(responseQRCodeData.data!!.qrCode, MediaTypeValue.IMAGE_PNG_VALUE.value)

    private fun logGenerationException(ex: Throwable) {
        logger.error(QR_GENERATION_FAILED_LOG_MESSAGE, ex.message, ex)
    }
}
