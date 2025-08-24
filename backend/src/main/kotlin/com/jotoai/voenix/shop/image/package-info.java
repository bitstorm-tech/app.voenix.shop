/**
 * Image module for managing image operations including storage, generation and conversion.
 * <p>
 * This module provides:
 * <ul>
 *   <li>Image CRUD operations through {@link com.jotoai.voenix.shop.image.api.ImageFacade}</li>
 *   <li>Image query operations and data access through {@link com.jotoai.voenix.shop.image.api.ImageQueryService}</li>
 *   <li>Image storage operations through {@link com.jotoai.voenix.shop.image.api.ImageStorageService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for image operations and queries</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code image::api} to access image functionality
.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Image Management"
)
package com.jotoai.voenix.shop.image;