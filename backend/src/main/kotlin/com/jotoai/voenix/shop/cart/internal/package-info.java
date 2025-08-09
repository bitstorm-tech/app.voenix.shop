/**
 * Internal implementation of the cart module.
 * This package and its sub-packages contain implementation details that should not be
 * accessed directly by other modules.
 *
 * <p>The internal structure includes:
 * - entity: JPA entities for cart data persistence
 * - repository: Data access layer
 * - service: Business logic implementations
 * - assembler: Entity-to-DTO conversion
 * - exception: Internal exception handling
 *
 * <p>Access to cart functionality should only go through the public API interfaces
 * defined in the 'api' package.
 */
package com.jotoai.voenix.shop.cart.internal;