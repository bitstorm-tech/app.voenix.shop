package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.vat.entity.ValueAddedTax
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "article_price_calculation")
data class CostCalculation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false, unique = true)
    val article: Article,
    // Purchase section fields
    @Column(name = "purchase_price_net", nullable = false)
    val purchasePriceNet: Int = 0,
    @Column(name = "purchase_price_tax", nullable = false)
    val purchasePriceTax: Int = 0,
    @Column(name = "purchase_price_gross", nullable = false)
    val purchasePriceGross: Int = 0,
    @Column(name = "purchase_cost_net", nullable = false)
    val purchaseCostNet: Int = 0,
    @Column(name = "purchase_cost_tax", nullable = false)
    val purchaseCostTax: Int = 0,
    @Column(name = "purchase_cost_gross", nullable = false)
    val purchaseCostGross: Int = 0,
    @Column(name = "purchase_cost_percent", precision = 5, scale = 2, nullable = false)
    val purchaseCostPercent: BigDecimal = BigDecimal.ZERO,
    @Column(name = "purchase_total_net", nullable = false)
    val purchaseTotalNet: Int = 0,
    @Column(name = "purchase_total_tax", nullable = false)
    val purchaseTotalTax: Int = 0,
    @Column(name = "purchase_total_gross", nullable = false)
    val purchaseTotalGross: Int = 0,
    @Column(name = "purchase_price_unit", length = 50, nullable = false)
    val purchasePriceUnit: String = "PER_PIECE",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_vat_rate_id")
    val purchaseVatRate: ValueAddedTax? = null,
    @Column(name = "purchase_vat_rate_percent", precision = 5, scale = 2, nullable = false)
    val purchaseVatRatePercent: BigDecimal = BigDecimal("19"),
    @Column(name = "purchase_calculation_mode", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    val purchaseCalculationMode: CalculationMode = CalculationMode.NET,
    // Sales section fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_vat_rate_id")
    val salesVatRate: ValueAddedTax? = null,
    @Column(name = "sales_vat_rate_percent", precision = 5, scale = 2, nullable = false)
    val salesVatRatePercent: BigDecimal = BigDecimal("19"),
    @Column(name = "sales_margin_net", nullable = false)
    val salesMarginNet: Int = 0,
    @Column(name = "sales_margin_tax", nullable = false)
    val salesMarginTax: Int = 0,
    @Column(name = "sales_margin_gross", nullable = false)
    val salesMarginGross: Int = 0,
    @Column(name = "sales_margin_percent", precision = 5, scale = 2, nullable = false)
    val salesMarginPercent: BigDecimal = BigDecimal.ZERO,
    @Column(name = "sales_total_net", nullable = false)
    val salesTotalNet: Int = 0,
    @Column(name = "sales_total_tax", nullable = false)
    val salesTotalTax: Int = 0,
    @Column(name = "sales_total_gross", nullable = false)
    val salesTotalGross: Int = 0,
    @Column(name = "sales_price_unit", length = 50, nullable = false)
    val salesPriceUnit: String = "PER_PIECE",
    @Column(name = "sales_calculation_mode", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    val salesCalculationMode: CalculationMode = CalculationMode.NET,
    // UI state fields
    @Column(name = "purchase_price_corresponds", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    val purchasePriceCorresponds: CalculationMode = CalculationMode.NET,
    @Column(name = "sales_price_corresponds", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    val salesPriceCorresponds: CalculationMode = CalculationMode.NET,
    @Column(name = "purchase_active_row", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    val purchaseActiveRow: PurchaseActiveRow = PurchaseActiveRow.COST,
    @Column(name = "sales_active_row", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    val salesActiveRow: SalesActiveRow = SalesActiveRow.MARGIN,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        com.jotoai.voenix.shop.domain.articles.dto.CostCalculationDto(
            id = requireNotNull(this.id) { "CostCalculation ID cannot be null when converting to DTO" },
            articleId = requireNotNull(this.article.id) { "Article ID cannot be null when converting to DTO" },
            purchasePriceNet = this.purchasePriceNet,
            purchasePriceTax = this.purchasePriceTax,
            purchasePriceGross = this.purchasePriceGross,
            purchaseCostNet = this.purchaseCostNet,
            purchaseCostTax = this.purchaseCostTax,
            purchaseCostGross = this.purchaseCostGross,
            purchaseCostPercent = this.purchaseCostPercent,
            purchaseTotalNet = this.purchaseTotalNet,
            purchaseTotalTax = this.purchaseTotalTax,
            purchaseTotalGross = this.purchaseTotalGross,
            purchasePriceUnit = this.purchasePriceUnit,
            purchaseVatRateId = this.purchaseVatRate?.id,
            purchaseVatRatePercent = this.purchaseVatRatePercent,
            purchaseCalculationMode = this.purchaseCalculationMode,
            salesVatRateId = this.salesVatRate?.id,
            salesVatRatePercent = this.salesVatRatePercent,
            salesMarginNet = this.salesMarginNet,
            salesMarginTax = this.salesMarginTax,
            salesMarginGross = this.salesMarginGross,
            salesMarginPercent = this.salesMarginPercent,
            salesTotalNet = this.salesTotalNet,
            salesTotalTax = this.salesTotalTax,
            salesTotalGross = this.salesTotalGross,
            salesPriceUnit = this.salesPriceUnit,
            salesCalculationMode = this.salesCalculationMode,
            purchasePriceCorresponds = this.purchasePriceCorresponds,
            salesPriceCorresponds = this.salesPriceCorresponds,
            purchaseActiveRow = this.purchaseActiveRow,
            salesActiveRow = this.salesActiveRow,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}

enum class CalculationMode {
    NET,
    GROSS,
}

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
