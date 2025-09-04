package com.jotoai.voenix.shop.vat

/**
 * Service interface for VAT module operations.
 * This interface provides both command and query operations for managing VAT configurations.
 * It serves as the primary API for both internal module operations and external module access to VAT functionality.
 */
interface VatService {
    /**
     * Checks if a VAT configuration exists by its ID.
     * This is the only cross-module requirement currently used by other modules.
     * @param id The VAT ID
     * @return true if the VAT exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
