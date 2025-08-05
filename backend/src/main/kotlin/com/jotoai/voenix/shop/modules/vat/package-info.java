/**
 * VAT (Value Added Tax) module for managing tax configurations.
 * <p>
 * This module provides:
 * <ul>
 *   <li>VAT CRUD operations through {@link com.jotoai.voenix.shop.modules.vat.api.VatFacade}</li>
 *   <li>VAT query operations and data access through {@link com.jotoai.voenix.shop.modules.vat.api.VatQueryService}</li>
 *   <li>Domain events for VAT lifecycle changes in the {@code events} package</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for VAT operations and queries</li>
 *   <li>{@code events} - Domain events published by the VAT module</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code vat::api} to access VAT functionality
 * and can listen to events from {@code vat::events} for reactive integration.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "VAT Management"
)
package com.jotoai.voenix.shop.modules.vat;