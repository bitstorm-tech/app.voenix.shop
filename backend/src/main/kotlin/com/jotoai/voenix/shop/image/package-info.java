/**
 * Image module for managing image operations including storage, generation and conversion.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Core image operations through {@link com.jotoai.voenix.shop.image.api.ImageOperations}</li>
 *   <li>Image storage and access through {@link com.jotoai.voenix.shop.image.api.ImageStorage}</li>
 *   <li>Image query operations and data access through {@link com.jotoai.voenix.shop.image.api.ImageQueryService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for image operations and queries</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code image::api} to access image functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Image Management"
)
package com.jotoai.voenix.shop.image;