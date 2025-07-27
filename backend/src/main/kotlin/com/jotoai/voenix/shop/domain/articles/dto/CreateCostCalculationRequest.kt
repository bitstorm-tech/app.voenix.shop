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

data class CreateCostCalculationRequest(
    // Purchase section fields
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceNet: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceTax: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchasePriceGross: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostNet: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostTax: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseCostGross: Int = 0,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val purchaseCostPercent: BigDecimal = BigDecimal.ZERO,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalNet: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalTax: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val purchaseTotalGross: Int = 0,
    @field:NotNull
    val purchasePriceUnit: String = "PER_PIECE",
    val purchaseVatRateId: Long? = null,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val purchaseVatRatePercent: BigDecimal = BigDecimal("19"),
    @field:NotNull
    val purchaseCalculationMode: CalculationMode = CalculationMode.NET,
    // Sales section fields
    val salesVatRateId: Long? = null,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    val salesVatRatePercent: BigDecimal = BigDecimal("19"),
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginNet")
    val salesMarginNet: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginTax")
    val salesMarginTax: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    @JsonAlias("marginGross")
    val salesMarginGross: Int = 0,
    @field:NotNull
    @field:DecimalMin("0.00")
    @field:DecimalMax("999.99")
    @JsonAlias("marginPercent")
    val salesMarginPercent: BigDecimal = BigDecimal.ZERO,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val salesTotalNet: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val salesTotalTax: Int = 0,
    @field:NotNull
    @field:Min(0)
    @field:Max(9999999999)
    val salesTotalGross: Int = 0,
    @field:NotNull
    val salesPriceUnit: String = "PER_PIECE",
    @field:NotNull
    val salesCalculationMode: CalculationMode = CalculationMode.NET,
    // UI state fields
    @field:NotNull
    val purchasePriceCorresponds: Any = CalculationMode.NET, // Can be Boolean or CalculationMode
    @field:NotNull
    val salesPriceCorresponds: Any = CalculationMode.NET, // Can be Boolean or CalculationMode
    @field:NotNull
    val purchaseActiveRow: PurchaseActiveRow = PurchaseActiveRow.COST,
    @field:NotNull
    val salesActiveRow: SalesActiveRow = SalesActiveRow.MARGIN,
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
