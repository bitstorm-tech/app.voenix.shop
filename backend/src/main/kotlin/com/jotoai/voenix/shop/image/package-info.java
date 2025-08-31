/**
 * Image module for managing image operations including storage, generation and conversion.
 * <p>
 * This module provides a unified API through:
 * <ul>
 *   <li>Unified image operations through {@link com.jotoai.voenix.shop.image.api.ImageService}</li>
 * </ul>
 * <p>
 * The ImageService consolidates all image-related operations that were previously
 * scattered across multiple interfaces (ImageOperations, ImageStorage, ImageQueryService).
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for unified image operations</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code image::api} to access image functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Image Management"
)
package com.jotoai.voenix.shop.image;