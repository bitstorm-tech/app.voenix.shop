package com.jotoai.voenix.shop.supplier.api

import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest

/**
 * Main facade for supplier module operations.
 * This interface defines all administrative operations for managing suppliers.
 */
interface SupplierFacade {
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
