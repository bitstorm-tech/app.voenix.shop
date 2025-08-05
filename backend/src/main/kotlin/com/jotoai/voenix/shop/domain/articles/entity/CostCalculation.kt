package com.jotoai.voenix.shop.domain.articles.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "article_price_calculation")
class CostCalculation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false, unique = true)
    var article: Article,
    // Purchase section fields
    @Column(name = "purchase_price_net", nullable = false)
    var purchasePriceNet: Int = 0,
    @Column(name = "purchase_price_tax", nullable = false)
    var purchasePriceTax: Int = 0,
    @Column(name = "purchase_price_gross", nullable = false)
    var purchasePriceGross: Int = 0,
    @Column(name = "purchase_cost_net", nullable = false)
    var purchaseCostNet: Int = 0,
    @Column(name = "purchase_cost_tax", nullable = false)
    var purchaseCostTax: Int = 0,
    @Column(name = "purchase_cost_gross", nullable = false)
    var purchaseCostGross: Int = 0,
    @Column(name = "purchase_cost_percent", precision = 5, scale = 2, nullable = false)
    var purchaseCostPercent: BigDecimal = BigDecimal.ZERO,
    @Column(name = "purchase_total_net", nullable = false)
    var purchaseTotalNet: Int = 0,
    @Column(name = "purchase_total_tax", nullable = false)
    var purchaseTotalTax: Int = 0,
    @Column(name = "purchase_total_gross", nullable = false)
    var purchaseTotalGross: Int = 0,
    @Column(name = "purchase_price_unit", length = 50, nullable = false)
    var purchasePriceUnit: String = "PER_PIECE",
    @Column(name = "purchase_vat_rate_id")
    var purchaseVatRateId: Long? = null,
    @Column(name = "purchase_vat_rate_percent", precision = 5, scale = 2, nullable = false)
    var purchaseVatRatePercent: BigDecimal = BigDecimal("19"),
    @Column(name = "purchase_calculation_mode", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    var purchaseCalculationMode: CalculationMode = CalculationMode.NET,
    // Sales section fields
    @Column(name = "sales_vat_rate_id")
    var salesVatRateId: Long? = null,
    @Column(name = "sales_vat_rate_percent", precision = 5, scale = 2, nullable = false)
    var salesVatRatePercent: BigDecimal = BigDecimal("19"),
    @Column(name = "sales_margin_net", nullable = false)
    var salesMarginNet: Int = 0,
    @Column(name = "sales_margin_tax", nullable = false)
    var salesMarginTax: Int = 0,
    @Column(name = "sales_margin_gross", nullable = false)
    var salesMarginGross: Int = 0,
    @Column(name = "sales_margin_percent", precision = 5, scale = 2, nullable = false)
    var salesMarginPercent: BigDecimal = BigDecimal.ZERO,
    @Column(name = "sales_total_net", nullable = false)
    var salesTotalNet: Int = 0,
    @Column(name = "sales_total_tax", nullable = false)
    var salesTotalTax: Int = 0,
    @Column(name = "sales_total_gross", nullable = false)
    var salesTotalGross: Int = 0,
    @Column(name = "sales_price_unit", length = 50, nullable = false)
    var salesPriceUnit: String = "PER_PIECE",
    @Column(name = "sales_calculation_mode", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    var salesCalculationMode: CalculationMode = CalculationMode.NET,
    // UI state fields
    @Column(name = "purchase_price_corresponds", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    var purchasePriceCorresponds: CalculationMode = CalculationMode.NET,
    @Column(name = "sales_price_corresponds", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    var salesPriceCorresponds: CalculationMode = CalculationMode.NET,
    @Column(name = "purchase_active_row", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var purchaseActiveRow: PurchaseActiveRow = PurchaseActiveRow.COST,
    @Column(name = "sales_active_row", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var salesActiveRow: SalesActiveRow = SalesActiveRow.MARGIN,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    var createdAt: OffsetDateTime? = null,
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
            purchaseVatRateId = this.purchaseVatRateId,
            purchaseVatRatePercent = this.purchaseVatRatePercent,
            purchaseCalculationMode = this.purchaseCalculationMode,
            salesVatRateId = this.salesVatRateId,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CostCalculation) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
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
