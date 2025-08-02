package com.jotoai.voenix.shop.domain.inventory.service

/**
 * Service interface for managing inventory stock levels.
 * This is a stub implementation until the full inventory system is developed.
 */
interface InventoryService {
    /**
     * Checks if the specified quantity of a variant is available in stock
     */
    fun isInStock(
        variantId: Long,
        quantity: Int,
    ): Boolean

    /**
     * Gets the current stock level for a variant
     */
    fun getStockLevel(variantId: Long): Int

    /**
     * Reserves stock for a cart item (decreases available stock temporarily)
     */
    fun reserveStock(
        variantId: Long,
        quantity: Int,
    ): Boolean

    /**
     * Releases reserved stock back to available inventory
     */
    fun releaseStock(
        variantId: Long,
        quantity: Int,
    )

    /**
     * Commits reserved stock (permanently removes from inventory, e.g., when order is placed)
     */
    fun commitStock(
        variantId: Long,
        quantity: Int,
    ): Boolean
}
