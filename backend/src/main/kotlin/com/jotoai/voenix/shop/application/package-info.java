/**
 * Application module providing shared utilities and cross-cutting concerns.
 * <p>
 * This module provides a clean API for shared components used across all other modules including
 * exception handling, DTOs, and configuration interfaces. It follows proper Spring Modulith architecture
 * with clear API boundaries and internal implementation details.
 * </p>
 * 
 * <h2>Module Architecture</h2>
 * <ul>
 *   <li><b>Public API:</b> {@code com.jotoai.voenix.shop.application.api} - Public interfaces and contracts</li>
 *   <li><b>Internal Implementation:</b> {@code com.jotoai.voenix.shop.application.internal} - Private implementation details</li>
 * </ul>
 * 
 * <h2>Public API</h2>
 * <ul>
 *   <li>{@code com.jotoai.voenix.shop.application.api.exception} - Base exception types and contracts</li>
 *   <li>{@code com.jotoai.voenix.shop.application.api.dto} - Common data transfer objects (ErrorResponse, PaginatedResponse)</li>
 * </ul>
 * 
 * <h2>Internal Implementation</h2>
 * <ul>
 *   <li>{@code com.jotoai.voenix.shop.application.internal.config} - Configuration implementations</li>
 *   <li>{@code com.jotoai.voenix.shop.application.internal.exception} - Exception handler implementations</li>
 * </ul>
 * 
 * <h2>Module Guidelines</h2>
 * <p>
 * Only truly cross-cutting concerns should be placed in this module:
 * </p>
 * <ul>
 *   <li>Base exception types extended by multiple modules</li>
 *   <li>Common DTOs for standard patterns (pagination, errors)</li>
 *   <li>Application-wide configuration properties</li>
 *   <li>Common exception handlers (for framework and validation exceptions)</li>
 * </ul>
 * <p>
 * Domain-specific exceptions and utilities should be placed in their respective modules.
 * Module-specific exception handlers should be implemented in each module with higher precedence.
 * </p>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Application Utilities",
        allowedDependencies = {}
)
package com.jotoai.voenix.shop.application;