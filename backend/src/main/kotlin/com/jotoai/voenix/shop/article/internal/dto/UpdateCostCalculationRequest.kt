package com.jotoai.voenix.shop.article.internal.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.jotoai.voenix.shop.article.internal.enum.CalculationMode
import com.jotoai.voenix.shop.article.internal.enum.PurchaseActiveRow
import com.jotoai.voenix.shop.article.internal.enum.SalesActiveRow
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdateCostCalculationRequest(
    // Purchase section fields
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchasePriceNet: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchasePriceTax: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchasePriceGross: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseCostNet: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseCostTax: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseCostGross: Int,
    @field:NotNull
    @field:DecimalMin(MIN_PERCENTAGE_VALUE)
    @field:DecimalMax(MAX_PERCENTAGE_VALUE)
    val purchaseCostPercent: BigDecimal,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseTotalNet: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseTotalTax: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val purchaseTotalGross: Int,
    @field:NotNull
    val purchasePriceUnit: String,
    val purchaseVatRateId: Long?,
    @field:NotNull
    @field:DecimalMin(MIN_PERCENTAGE_VALUE)
    @field:DecimalMax(MAX_PERCENTAGE_VALUE)
    val purchaseVatRatePercent: BigDecimal,
    @field:NotNull
    val purchaseCalculationMode: CalculationMode,
    // Sales section fields
    val salesVatRateId: Long?,
    @field:NotNull
    @field:DecimalMin(MIN_PERCENTAGE_VALUE)
    @field:DecimalMax(MAX_PERCENTAGE_VALUE)
    val salesVatRatePercent: BigDecimal,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    @JsonAlias("marginNet")
    val salesMarginNet: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    @JsonAlias("marginTax")
    val salesMarginTax: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    @JsonAlias("marginGross")
    val salesMarginGross: Int,
    @field:NotNull
    @field:DecimalMin(MIN_PERCENTAGE_VALUE)
    @field:DecimalMax(MAX_PERCENTAGE_VALUE)
    @JsonAlias("marginPercent")
    val salesMarginPercent: BigDecimal,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val salesTotalNet: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val salesTotalTax: Int,
    @field:NotNull
    @field:Min(MIN_MONETARY_VALUE_CENTS)
    @field:Max(MAX_MONETARY_VALUE_CENTS)
    val salesTotalGross: Int,
    @field:NotNull
    val salesPriceUnit: String,
    @field:NotNull
    val salesCalculationMode: CalculationMode,
    // UI state fields
    @field:NotNull
    val purchasePriceCorresponds: Any, // Can be Boolean or CalculationMode
    @field:NotNull
    val salesPriceCorresponds: Any, // Can be Boolean or CalculationMode
    @field:NotNull
    val purchaseActiveRow: PurchaseActiveRow,
    @field:NotNull
    val salesActiveRow: SalesActiveRow,
) {
    companion object {
        /** Minimum monetary value in cents (0 cents) */
        const val MIN_MONETARY_VALUE_CENTS: Long = 0L

        /** Maximum monetary value in cents (99,999,999.99 EUR) */
        const val MAX_MONETARY_VALUE_CENTS: Long = 9999999999L

        /** Minimum percentage value for VAT rates and margins */
        const val MIN_PERCENTAGE_VALUE: String = "0.00"

        /** Maximum percentage value for VAT rates and margins (999.99%) */
        const val MAX_PERCENTAGE_VALUE: String = "999.99"
    }

    fun getPurchasePriceCorrespondsAsEnum(): CalculationMode =
        when (purchasePriceCorresponds) {
            is Boolean -> if (purchasePriceCorresponds) CalculationMode.GROSS else CalculationMode.NET
            is String -> CalculationMode.valueOf(purchasePriceCorresponds)
            is CalculationMode -> purchasePriceCorresponds
            else -> throw IllegalArgumentException(
                "Invalid type for purchasePriceCorresponds: ${purchasePriceCorresponds::class.simpleName}",
            )
        }

    fun getSalesPriceCorrespondsAsEnum(): CalculationMode =
        when (salesPriceCorresponds) {
            is Boolean -> if (salesPriceCorresponds) CalculationMode.GROSS else CalculationMode.NET
            is String -> CalculationMode.valueOf(salesPriceCorresponds)
            is CalculationMode -> salesPriceCorresponds
            else -> throw IllegalArgumentException(
                "Invalid type for salesPriceCorresponds: ${salesPriceCorresponds::class.simpleName}",
            )
        }
}
