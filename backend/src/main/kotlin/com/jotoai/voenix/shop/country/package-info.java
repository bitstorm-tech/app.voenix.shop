/**
 * Country module for managing country data and operations.
 * 
 * This module provides country management functionality including:
 * - Country creation, updating, and deletion
 * - Country querying services
 * - Event publishing for country lifecycle changes
 * 
 * <p>The module follows Spring Modulith architecture principles with clear
 * separation between public API (exposed through the api package) and
 * internal implementation details (in the internal package).</p>
 * 
 * <p>External modules can interact with this module through:
 * <ul>
 *   <li>{@link com.jotoai.voenix.shop.country.api.CountryFacade} - Main administrative operations</li>
 *   <li>{@link com.jotoai.voenix.shop.country.api.CountryQueryService} - Query operations</li>
 *   <li>Events published from {@link com.jotoai.voenix.shop.country.events} package</li>
 * </ul>
 * </p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Country Management"
)
package com.jotoai.voenix.shop.country;