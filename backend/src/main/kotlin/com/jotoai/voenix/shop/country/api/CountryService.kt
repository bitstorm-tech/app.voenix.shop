package com.jotoai.voenix.shop.country.api

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException

/**
 * Unified service interface for Country module operations.
 * This interface defines all operations for managing and querying countries.
 */
interface CountryService {
    /**
     * Retrieves all countries.
     */
    fun getAllCountries(): List<CountryDto>

    /**
     * Retrieves a country by its ID.
     * @param id The country ID
     * @return The country
     * @throws CountryNotFoundException if the country is not found
     */
    fun getCountryById(id: Long): CountryDto

    /**
     * Checks if a country exists by its ID.
     * @param id The country ID
     * @return true if the country exists, false otherwise
     */
    fun existsById(id: Long): Boolean

}
