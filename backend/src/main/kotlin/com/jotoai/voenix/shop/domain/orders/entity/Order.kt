package com.jotoai.voenix.shop.domain.orders.entity

import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.domain.orders.enums.OrderStatus
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(name = "order_number", unique = true, length = 50, insertable = false, updatable = false)
    var orderNumber: String? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "customer_email", nullable = false, length = 255)
    var customerEmail: String,
    @Column(name = "customer_first_name", nullable = false, length = 255)
    var customerFirstName: String,
    @Column(name = "customer_last_name", nullable = false, length = 255)
    var customerLastName: String,
    @Column(name = "customer_phone", nullable = true, length = 50)
    var customerPhone: String? = null,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress1", column = Column(name = "shipping_street_address_1")),
        AttributeOverride(name = "streetAddress2", column = Column(name = "shipping_street_address_2")),
        AttributeOverride(name = "city", column = Column(name = "shipping_city")),
        AttributeOverride(name = "state", column = Column(name = "shipping_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "shipping_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "shipping_country")),
    )
    var shippingAddress: Address,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress1", column = Column(name = "billing_street_address_1")),
        AttributeOverride(name = "streetAddress2", column = Column(name = "billing_street_address_2")),
        AttributeOverride(name = "city", column = Column(name = "billing_city")),
        AttributeOverride(name = "state", column = Column(name = "billing_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "billing_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "billing_country")),
    )
    var billingAddress: Address? = null,
    @Column(name = "subtotal", nullable = false)
    var subtotal: Long, // In cents
    @Column(name = "tax_amount", nullable = false)
    var taxAmount: Long, // In cents
    @Column(name = "shipping_amount", nullable = false)
    var shippingAmount: Long, // In cents
    @Column(name = "total_amount", nullable = false)
    var totalAmount: Long, // In cents
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PENDING,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart,
    @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
    var notes: String? = null,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @BatchSize(size = 20)
    var items: MutableList<OrderItem> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    /**
     * Gets the total number of items in the order
     */
    fun getTotalItemCount(): Int = items.sumOf { it.quantity }

    /**
     * Checks if the order can be cancelled
     */
    fun canBeCancelled(): Boolean = status == OrderStatus.PENDING

    /**
     * Gets the customer's full name
     */
    fun getCustomerFullName(): String = "$customerFirstName $customerLastName"

    /**
     * Adds an item to the order
     */
    fun addItem(item: OrderItem) {
        item.order = this
        items.add(item)
    }

    /**
     * Calculates the total price from all items (should match totalAmount)
     */
    fun calculateItemsTotal(): Long = items.sumOf { it.totalPrice }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()
}
