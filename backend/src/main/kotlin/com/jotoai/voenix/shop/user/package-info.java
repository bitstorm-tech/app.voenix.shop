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
 *   <li><b>Events:</b> {@code com.jotoai.voenix.shop.user.events} - Domain events for user lifecycle</li>
 *   <li><b>Internal:</b> {@code com.jotoai.voenix.shop.user.internal} - Private implementation details</li>
 * </ul>
 * 
 * <h2>Key Services</h2>
 * <ul>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserFacade} - User management operations (create, update, delete)</li>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserQueryService} - User query operations (read-only)</li>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserAuthenticationService} - Authentication support</li>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserPasswordService} - Password management</li>
 *   <li>{@link com.jotoai.voenix.shop.user.api.UserRoleManagementService} - Role assignment and management</li>
 * </ul>
 * 
 * <h2>Events Published</h2>
 * <ul>
 *   <li>{@code UserCreatedEvent} - When a new user is created</li>
 *   <li>{@code UserUpdatedEvent} - When user data is modified</li>
 *   <li>{@code UserDeletedEvent} - When a user is deleted (soft or hard)</li>
 *   <li>{@code RoleAssignedEvent} - When roles are assigned to a user</li>
 *   <li>{@code RoleRevokedEvent} - When roles are removed from a user</li>
 * </ul>
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
        displayName = "User Management"
)
package com.jotoai.voenix.shop.user;