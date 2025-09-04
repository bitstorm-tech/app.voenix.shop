package com.jotoai.voenix.shop.article.internal.enum

enum class SalesActiveRow {
    MARGIN,
    MARGIN_PERCENT,
    TOTAL,
    ;

    companion object {
        @JvmStatic
        @com.fasterxml.jackson.annotation.JsonCreator
        fun fromValue(value: String): SalesActiveRow =
            when (value.lowercase()) {
                "margin" -> MARGIN
                "marginpercent", "margin_percent" -> MARGIN_PERCENT
                "total" -> TOTAL
                else -> throw IllegalArgumentException("Unknown SalesActiveRow value: $value")
            }
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue(): String =
        when (this) {
            MARGIN -> "margin"
            MARGIN_PERCENT -> "marginPercent"
            TOTAL -> "total"
        }
}
