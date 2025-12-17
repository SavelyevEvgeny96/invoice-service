package ru.sogaz.site.orderingService.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class BankEnum(
    @JsonValue val code: String,
    val description: String,
) {
    GPB("gpb", "ГПБ"),
    AKB_RUS("akb_rus", "АБР"),
    ;

    companion object {
        fun from(value: String?): BankEnum? = BankEnum.entries.find { it.code == value }
    }
}
