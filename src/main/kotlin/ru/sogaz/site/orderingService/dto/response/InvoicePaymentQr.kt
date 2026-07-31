package ru.sogaz.site.orderingService.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class InvoicePaymentQr(
    @get:JsonProperty("QRInfo")
    var qrInfo: PaymentQrInfo = PaymentQrInfo(),
    var detais: PaymentDetails = PaymentDetails(),
    var infoInvoice: PaymentInvoiceInfo = PaymentInvoiceInfo(),
)

data class PaymentQrInfo(
    var contentQR: String = "",
    var mediaType: String = "",
)

data class PaymentDetails(
    var payerFio: PaymentPayerFio = PaymentPayerFio(),
    var recipient: PaymentRecipient = PaymentRecipient(),
    var bank: PaymentBank = PaymentBank(),
)

data class PaymentPayerFio(
    var lastName: String = "",
    var firstName: String = "",
    var middleName: String = "",
)

data class PaymentRecipient(
    var name: String = "",
    var inn: String = "",
    var kpp: String = "",
    var personalAcc: String = "",
    var purposePayment: String = "",
)

data class PaymentBank(
    var name: String = "",
    var bic: String = "",
    var corresAcc: String = "",
)

data class PaymentInvoiceInfo(
    var premiumAmount: String = "",
    var accounts: PaymentAccount = PaymentAccount(),
    var urlSuccess: String = "",
)

data class PaymentAccount(
    var agreementNumber: String = "",
    var insuranceKind: String = "",
    var agreementPrice: String = "",
)
