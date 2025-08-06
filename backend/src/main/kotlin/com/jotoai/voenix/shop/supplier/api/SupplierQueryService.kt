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
}
