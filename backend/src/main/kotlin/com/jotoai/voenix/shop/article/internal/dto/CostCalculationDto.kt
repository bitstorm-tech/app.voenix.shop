package com.jotoai.voenix.shop.article.internal.dto

import com.jotoai.voenix.shop.article.internal.enum.CalculationMode
import com.jotoai.voenix.shop.article.internal.enum.PurchaseActiveRow
import com.jotoai.voenix.shop.article.internal.enum.SalesActiveRow
import java.math.BigDecimal
import java.time.OffsetDateTime

data class CostCalculationDto(
    val id: Long,
    val articleId: Long,
    // Purchase section fields
    val purchasePriceNet: Int,
    val purchasePriceTax: Int,
    val purchasePriceGross: Int,
    val purchaseCostNet: Int,
    val purchaseCostTax: Int,
    val purchaseCostGross: Int,
    val purchaseCostPercent: BigDecimal,
    val purchaseTotalNet: Int,
    val purchaseTotalTax: Int,
    val purchaseTotalGross: Int,
    val purchasePriceUnit: String,
    val purchaseVatRateId: Long?,
    val purchaseVatRatePercent: BigDecimal,
    val purchaseCalculationMode: CalculationMode,
    // Sales section fields
    val salesVatRateId: Long?,
    val salesVatRatePercent: BigDecimal,
    val salesMarginNet: Int,
    val salesMarginTax: Int,
    val salesMarginGross: Int,
    val salesMarginPercent: BigDecimal,
    val salesTotalNet: Int,
    val salesTotalTax: Int,
    val salesTotalGross: Int,
    val salesPriceUnit: String,
    val salesCalculationMode: CalculationMode,
    // UI state fields
    val purchasePriceCorresponds: CalculationMode,
    val salesPriceCorresponds: CalculationMode,
    val purchaseActiveRow: PurchaseActiveRow,
    val salesActiveRow: SalesActiveRow,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
