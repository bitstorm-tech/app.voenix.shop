/**
 * Order Management module for handling order operations.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Order creation and lifecycle management through {@link com.jotoai.voenix.shop.order.api.OrderFacade}</li>
 *   <li>Order query operations and data access through {@link com.jotoai.voenix.shop.order.api.OrderQueryService}</li>
 *   <li>Order status management and business rules</li>
 *   <li>Address management for shipping and billing</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for order operations and queries</li>
 *   <li>{@code web} - REST controllers for order management</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code order::api} to access order functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Order Management"
)
package com.jotoai.voenix.shop.order;