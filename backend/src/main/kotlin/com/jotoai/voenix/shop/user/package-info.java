/**
 * User management module.
 * <p>
 * This module provides all user-related functionality including user creation,
 * updates, authentication support, and query operations. It follows Spring Modulith
 * architecture with clear separation between public API and internal implementation.
 * </p>
 * 
 * <h2>Module Boundaries</h2>
 * <ul>
 *   <li><b>Public API:</b> {@code com.jotoai.voenix.shop.user.api} - Service interfaces and DTOs</li>
 *   <li><b>Internal:</b> {@code com.jotoai.voenix.shop.user.internal} - Private implementation details</li>
 * </ul>
 * 
 * <h2>Key Services</h2>
 * <ul>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserService} - Unified service for all user operations</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <p>
 * This module follows a simplified architecture with a single {@code UserService} that provides
 * all user-related operations including:
 * </p>
 * <ul>
 *   <li>User management (create, update, delete, restore)</li>
 *   <li>User queries and search</li>
 *   <li>Authentication support</li>
 *   <li>Password management with proper BCrypt encoding</li>
 *   <li>Role assignment and management</li>
 *   <li>Bulk operations for efficiency</li>
 * </ul>
 * 
 * 
 * <h2>Module Dependencies</h2>
 * <ul>
 *   <li>No dependencies on other domain modules</li>
 *   <li>Uses common module for shared exceptions</li>
 *   <li>Spring Security for password encoding</li>
 * </ul>
 * 
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li>Passwords are always encoded using BCrypt</li>
 *   <li>Soft delete support for user data retention</li>
 *   <li>Role-based access control through Role entity</li>
 *   <li>Email validation and uniqueness constraints</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "User Management",
        allowedDependencies = {"common"}
)
package com.jotoai.voenix.shop.user;