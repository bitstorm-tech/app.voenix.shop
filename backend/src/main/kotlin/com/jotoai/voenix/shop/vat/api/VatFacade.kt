package com.jotoai.voenix.shop.vat.api

import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto

/**
 * Main facade for VAT module operations.
 * This interface defines all administrative operations for managing VAT configurations.
 */
interface VatFacade {
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
