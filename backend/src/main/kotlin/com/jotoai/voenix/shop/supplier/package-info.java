/**
 * Supplier module for managing supplier information.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Unified supplier operations (both read and write) through {@link com.jotoai.voenix.shop.supplier.api.SupplierService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for supplier operations and queries</li>
 *   <li>{@code web} - REST controllers for supplier endpoints</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code supplier::api} to access supplier functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Supplier Management"
)
package com.jotoai.voenix.shop.supplier;