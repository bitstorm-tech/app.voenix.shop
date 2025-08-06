package com.jotoai.voenix.shop.country.api

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.dto.CreateCountryRequest
import com.jotoai.voenix.shop.country.api.dto.UpdateCountryRequest

/**
 * Main facade for Country module operations.
 * This interface defines all administrative operations for managing countries.
 */
interface CountryFacade {
    /**
     * Creates a new country.
     */
    fun createCountry(request: CreateCountryRequest): CountryDto

    /**
     * Updates an existing country.
     */
    fun updateCountry(
        id: Long,
        request: UpdateCountryRequest,
    ): CountryDto

    /**
     * Deletes a country.
     */
    fun deleteCountry(id: Long)
}
