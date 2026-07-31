package ru.sogaz.site.orderingService.dto.data

data class PaymentQrData(
    val name: String,
    val personalAcc: String,
    val bankName: String,
    val bic: String,
    val correspAcc: String,
    val sum: String,
    val payeeInn: String,
    val kpp: String,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val contractNumber: String,
    val contractDate: String,
)
