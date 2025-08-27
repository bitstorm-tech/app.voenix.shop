/**
 * Country module for managing country data and operations.
 * 
 * This module provides country querying functionality including:
 * - Listing available countries
 * - Retrieving countries by id
 * - Existence checks for validation
 * 
 * <p>The module follows Spring Modulith architecture principles with clear
 * separation between public API (exposed through the api package) and
 * internal implementation details (in the internal package).</p>
 * 
 * <p>External modules can interact with this module through:
 * <ul>
 *   <li>{@link com.jotoai.voenix.shop.country.api.CountryService} - Country query operations</li>
 * </ul>
 * </p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Country Management"
)
package com.jotoai.voenix.shop.country;
