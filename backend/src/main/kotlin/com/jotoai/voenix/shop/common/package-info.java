/**
 * Common module providing shared utilities and cross-cutting concerns.
 * <p>
 * This module contains shared components used across all other modules including
 * exception handling, DTOs, configuration, and utilities.
 * </p>
 * 
 * <h2>Module Boundaries</h2>
 * <ul>
 *   <li><b>Exceptions:</b> {@code com.jotoai.voenix.shop.common.exception} - Shared exception types</li>
 *   <li><b>DTOs:</b> {@code com.jotoai.voenix.shop.common.dto} - Common data transfer objects</li>
 *   <li><b>Config:</b> {@code com.jotoai.voenix.shop.common.config} - Shared configuration</li>
 * </ul>
 * 
 * <h2>Exposed Types</h2>
 * <ul>
 *   <li>{@code ResourceNotFoundException} - Used when resources cannot be found</li>
 *   <li>{@code ResourceAlreadyExistsException} - Used for duplicate resource creation</li>
 *   <li>{@code BadRequestException} - Used for invalid request data</li>
 *   <li>{@code PaginatedResponse} - Common pagination wrapper</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Common Utilities",
        allowedDependencies = {}
)
@org.springframework.modulith.ApplicationModule.Type(
        org.springframework.modulith.ApplicationModule.Type.Value.OPEN
)
package com.jotoai.voenix.shop.common;