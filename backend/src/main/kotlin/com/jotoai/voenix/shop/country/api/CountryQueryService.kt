package com.jotoai.voenix.shop.country.api

import com.jotoai.voenix.shop.country.api.dto.CountryDto

/**
 * Query service for Country module read operations.
 * This interface defines all read-only operations for country data.
 * It serves as the primary read API for other modules to access country information.
 */
interface CountryQueryService {
    /**
     * Retrieves all countries.
     */
    fun getAllCountries(): List<CountryDto>

    /**
     * Retrieves a country by its ID.
     * @param id The country ID
     * @return The country
     * @throws RuntimeException if the country is not found
     */
    fun getCountryById(id: Long): CountryDto

    /**
     * Checks if a country exists by its ID.
     * @param id The country ID
     * @return true if the country exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Retrieves a country entity reference by its ID for legacy entity relationships.
     * This method is intended for backward compatibility with existing JPA entities
     * that have direct foreign key relationships to Country.
     *
     * @param id The country ID
     * @return The country entity reference
     * @throws RuntimeException if the country is not found
     * @deprecated This method should be removed once all entity relationships are refactored
     */
    @Deprecated("Use getCountryById instead and refactor entity relationships")
    fun getCountryEntityReference(id: Long): Any
}
