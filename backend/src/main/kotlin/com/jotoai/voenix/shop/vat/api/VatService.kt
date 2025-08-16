package com.jotoai.voenix.shop.vat.api

import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto

/**
 * Service interface for VAT module operations.
 * This interface provides both command and query operations for managing VAT configurations.
 * It serves as the primary API for both internal module operations and external module access to VAT functionality.
 */
interface VatService {
    /**
     * Retrieves all VAT configurations.
     */
    fun getAllVats(): List<ValueAddedTaxDto>

    /**
     * Retrieves a VAT configuration by its ID.
     * @param id The VAT ID
     * @return The VAT configuration
     * @throws RuntimeException if the VAT is not found
     */
    fun getVatById(id: Long): ValueAddedTaxDto

    /**
     * Retrieves the default VAT configuration.
     * @return The default VAT configuration, or null if no default is set.
     */
    fun getDefaultVat(): ValueAddedTaxDto?

    /**
     * Checks if a VAT configuration exists by its ID.
     * @param id The VAT ID
     * @return true if the VAT exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Creates a new VAT configuration.
     */
    fun createVat(request: CreateValueAddedTaxRequest): ValueAddedTaxDto

    /**
     * Updates an existing VAT configuration.
     */
    fun updateVat(
        id: Long,
        request: UpdateValueAddedTaxRequest,
    ): ValueAddedTaxDto

    /**
     * Deletes a VAT configuration.
     */
    fun deleteVat(id: Long)
}
