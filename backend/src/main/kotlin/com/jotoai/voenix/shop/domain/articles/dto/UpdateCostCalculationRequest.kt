package com.jotoai.voenix.shop.domain.articles.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.jotoai.voenix.shop.domain.articles.entity.CalculationMode
import com.jotoai.voenix.shop.domain.articles.entity.PurchaseActiveRow
import com.jotoai.voenix.shop.domain.articles.entity.SalesActiveRow
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdateCostCalculationRequest(
    // Purchase section fields
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceNet: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceTax: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceGross: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostNet: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostTax: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostGross: Int,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val purchaseCostPercent: BigDecimal,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalNet: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalTax: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalGross: Int,
    @field:NotNull
    val purchasePriceUnit: String,
    val purchaseVatRateId: Long?,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val purchaseVatRatePercent: BigDecimal,
    @field:NotNull
    val purchaseCalculationMode: CalculationMode,
    // Sales section fields
    val salesVatRateId: Long?,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val salesVatRatePercent: BigDecimal,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginNet")
    val salesMarginNet: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginTax")
    val salesMarginTax: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginGross")
    val salesMarginGross: Int,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    @JsonAlias("marginPercent")
    val salesMarginPercent: BigDecimal,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val salesTotalNet: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val salesTotalTax: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
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
