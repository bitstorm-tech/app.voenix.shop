package com.jotoai.voenix.shop.article.internal.enum

enum class PurchaseActiveRow {
    COST,
    COST_PERCENT,
    ;

    companion object {
        @JvmStatic
        @com.fasterxml.jackson.annotation.JsonCreator
        fun fromValue(value: String): PurchaseActiveRow =
            when (value.lowercase()) {
                "cost" -> COST
                "costpercent", "cost_percent" -> COST_PERCENT
                else -> throw IllegalArgumentException("Unknown PurchaseActiveRow value: $value")
            }
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue(): String =
        when (this) {
            COST -> "cost"
            COST_PERCENT -> "costPercent"
        }
}
