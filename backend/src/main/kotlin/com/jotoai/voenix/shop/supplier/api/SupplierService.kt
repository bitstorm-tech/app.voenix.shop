package com.jotoai.voenix.shop.supplier.api

import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest

/**
 * Unified service interface for supplier module operations.
 * This interface combines both read and write operations for managing suppliers,
 * replacing the previous CQRS pattern of separate facade and query service interfaces.
 */
interface SupplierService {
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
     * Creates a new supplier.
     */
    fun createSupplier(request: CreateSupplierRequest): SupplierDto

    /**
     * Updates an existing supplier.
     */
    fun updateSupplier(
        id: Long,
        request: UpdateSupplierRequest,
    ): SupplierDto

    /**
     * Deletes a supplier.
     */
    fun deleteSupplier(id: Long)
}
