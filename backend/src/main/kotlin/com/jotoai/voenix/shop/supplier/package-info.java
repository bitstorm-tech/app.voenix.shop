/**
 * Supplier module for managing supplier information.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Supplier CRUD operations through {@link com.jotoai.voenix.shop.supplier.api.SupplierFacade}</li>
 *   <li>Supplier query operations and data access through {@link com.jotoai.voenix.shop.supplier.api.SupplierQueryService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for supplier operations and queries</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code supplier::api} to access supplier functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Supplier Management"
)
package com.jotoai.voenix.shop.supplier;