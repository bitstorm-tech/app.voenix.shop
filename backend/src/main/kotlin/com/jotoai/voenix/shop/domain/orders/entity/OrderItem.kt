package com.jotoai.voenix.shop.domain.orders.entity

import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    @Column(name = "article_id", nullable = false)
    var articleId: Long,
    @Column(name = "variant_id", nullable = false)
    var variantId: Long,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "price_per_item", nullable = false)
    var pricePerItem: Long, // Price in cents at time of order
    @Column(name = "total_price", nullable = false)
    var totalPrice: Long, // Total price for this line item (pricePerItem * quantity)
    @Column(name = "generated_image_id", nullable = true)
    var generatedImageId: Long? = null,
    @Column(name = "generated_image_filename", nullable = true, length = 255)
    var generatedImageFilename: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = true)
    var prompt: Prompt? = null,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", nullable = false, columnDefinition = "jsonb")
    var customData: Map<String, Any> = emptyMap(), // Crop info, etc.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
) {
    /**
     * Validates that the total price matches quantity * pricePerItem
     */
    fun validatePricing(): Boolean = totalPrice == (pricePerItem * quantity)

    /**
     * Recalculates and updates the total price
     */
    fun recalculateTotalPrice() {
        totalPrice = pricePerItem * quantity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()
}
