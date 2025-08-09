/**
 * Cart module for managing shopping cart functionality.
 * This module provides capabilities for adding, updating, removing items in shopping carts,
 * cart price calculations, and cart-to-order conversion.
 *
 * <p>This module is designed following Spring Modulith architectural principles:
 * - Public API in the 'api' package is exposed to other modules
 * - Internal implementation details are hidden in 'internal' packages
 * - Clear separation of concerns between command and query operations
 *
 * <p>Public interfaces:
 * - CartFacade: Command operations (add, update, remove items)
 * - CartQueryService: Query operations (retrieve cart data)
 *
 * <p>Integration points:
 * - Depends on: user, article, image, prompt modules
 * - Used by: order processing, user interfaces
 */
@org.springframework.modulith.ApplicationModule
package com.jotoai.voenix.shop.cart;