/**
 * Supplier module for managing supplier information.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Supplier CRUD operations through {@link com.jotoai.voenix.shop.supplier.api.SupplierFacade}</li>
 *   <li>Supplier query operations and data access through {@link com.jotoai.voenix.shop.supplier.api.SupplierQueryService}</li>
 *   <li>Domain events for supplier lifecycle changes in the {@code events} package</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for supplier operations and queries</li>
 *   <li>{@code events} - Domain events published by the supplier module</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code supplier::api} to access supplier functionality
 * and can listen to events from {@code supplier::events} for reactive integration.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Supplier Management"
)
package com.jotoai.voenix.shop.supplier;