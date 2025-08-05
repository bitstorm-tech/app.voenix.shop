package com.jotoai.voenix.shop.supplier.api

import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto

/**
 * Query service for supplier module read operations.
 * This interface defines all read-only operations for supplier data.
 * It serves as the primary read API for other modules to access supplier information.
 */
interface SupplierQueryService {
    /**
     * Retrieves all suppliers.
     */
    fun getAllSuppliers(): List<SupplierDto>

    /**
     * Retrieves a supplier by its ID.
     * @param id The supplier ID
     * @return The supplier information
     * @throws RuntimeException if the supplier is not found
     */
    fun getSupplierById(id: Long): SupplierDto

    /**
     * Checks if a supplier exists by its ID.
     * @param id The supplier ID
     * @return true if the supplier exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Retrieves a supplier entity reference by its ID for legacy entity relationships.
     * This method is intended for backward compatibility with existing JPA entities
     * that have direct foreign key relationships to Supplier.
     *
     * @param id The supplier ID
     * @return The supplier entity reference
     * @throws RuntimeException if the supplier is not found
     * @deprecated This method should be removed once all entity relationships are refactored
     */
    @Deprecated("Use getSupplierById instead and refactor entity relationships")
    fun getSupplierEntityReference(id: Long): Any
}
