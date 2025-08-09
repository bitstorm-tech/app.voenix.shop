/**
 * Common module providing shared utilities and cross-cutting concerns.
 * <p>
 * This module contains shared components used across all other modules including
 * exception handling, DTOs, configuration, and utilities. As an OPEN module type,
 * it can be accessed by all other modules without explicit dependency declaration.
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
 *   <li>{@code ErrorResponse} - Standard error response format</li>
 *   <li>{@code GlobalExceptionHandler} - Application-wide exception handling</li>
 * </ul>
 * 
 * <h2>Module Guidelines</h2>
 * <p>
 * Only truly cross-cutting concerns should be placed in this module:
 * </p>
 * <ul>
 *   <li>Generic exceptions used by multiple modules</li>
 *   <li>Common DTOs for standard patterns (pagination, errors)</li>
 *   <li>Application-wide configuration (CORS, caching, etc.)</li>
 *   <li>Global exception handlers</li>
 * </ul>
 * <p>
 * Domain-specific exceptions and utilities should be placed in their respective modules.
 * For example, PdfGenerationException belongs in the pdf module, not here.
 * </p>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Common Utilities",
        allowedDependencies = {},
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.jotoai.voenix.shop.common;