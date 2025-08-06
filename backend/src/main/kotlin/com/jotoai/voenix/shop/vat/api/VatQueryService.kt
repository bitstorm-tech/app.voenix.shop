package com.jotoai.voenix.shop.vat.api

import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto

/**
 * Query service for VAT module read operations.
 * This interface defines all read-only operations for VAT data.
 * It serves as the primary read API for other modules to access VAT information.
 */
interface VatQueryService {
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
}
