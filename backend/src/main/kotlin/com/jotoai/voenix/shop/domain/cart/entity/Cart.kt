package com.jotoai.voenix.shop.domain.cart.entity

import com.jotoai.voenix.shop.domain.cart.enums.CartStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "carts")
class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "status", nullable = false, length = 20)
    var status: CartStatus = CartStatus.ACTIVE,
    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,
    @Column(name = "expires_at", columnDefinition = "timestamptz")
    var expiresAt: OffsetDateTime? = null,
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC, createdAt ASC")
    @BatchSize(size = 20)
    var items: MutableList<CartItem> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    /**
     * Gets the total number of items in the cart
     */
    fun getTotalItemCount(): Int = items.sumOf { it.quantity }

    /**
     * Gets the total price of all items in the cart (in cents)
     */
    fun getTotalPrice(): Long = items.sumOf { it.priceAtTime * it.quantity }

    /**
     * Checks if the cart is empty
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Adds an item to the cart or updates quantity if item already exists
     */
    fun addOrUpdateItem(item: CartItem) {
        val existingItem =
            items.find {
                it.articleId == item.articleId &&
                    it.variantId == item.variantId &&
                    it.customData == item.customData
            }

        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            item.cart = this
            item.position = items.size
            items.add(item)
        }
    }

    /**
     * Removes an item from the cart
     */
    fun removeItem(itemId: Long): Boolean {
        val removed = items.removeIf { it.id == itemId }
        if (removed) {
            // Reorder positions
            items.forEachIndexed { index, item ->
                item.position = index
            }
        }
        return removed
    }

    /**
     * Clears all items from the cart
     */
    fun clearItems() {
        items.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cart) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()
}
