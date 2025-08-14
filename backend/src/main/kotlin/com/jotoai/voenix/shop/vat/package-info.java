/**
 * VAT (Value Added Tax) module for managing tax configurations.
 * <p>
 * This module provides:
 * <ul>
 *   <li>VAT operations and data access through {@link com.jotoai.voenix.shop.vat.api.VatService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for VAT operations and queries</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code vat::api} to access VAT functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "VAT Management"
)
package com.jotoai.voenix.shop.vat;