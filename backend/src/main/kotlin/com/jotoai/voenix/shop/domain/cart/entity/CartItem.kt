package com.jotoai.voenix.shop.domain.cart.entity

import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.prompts.entity.Prompt
import com.jotoai.voenix.shop.image.internal.domain.GeneratedImage
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
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    var variant: MugArticleVariant,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "price_at_time", nullable = false)
    var priceAtTime: Long, // Price in cents when item was added
    @Column(name = "original_price", nullable = false)
    var originalPrice: Long, // Original price for comparison
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", nullable = false, columnDefinition = "jsonb")
    var customData: Map<String, Any> = emptyMap(), // Only for crop data and similar non-FK fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_image_id", nullable = true)
    var generatedImage: GeneratedImage? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = true)
    var prompt: Prompt? = null,
    @Column(name = "position", nullable = false)
    var position: Int = 0,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    /**
     * Gets the total price for this cart item (price * quantity)
     */
    fun getTotalPrice(): Long = priceAtTime * quantity

    /**
     * Checks if the current price has changed from when the item was added
     */
    fun hasPriceChanged(): Boolean = priceAtTime != originalPrice

    /**
     * Updates the quantity and ensures it's positive
     */
    fun updateQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "Quantity must be positive" }
        quantity = newQuantity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()
}
