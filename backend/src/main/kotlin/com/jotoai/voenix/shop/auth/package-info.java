/**
 * Authentication and authorization module.
 * <p>
 * This module provides authentication services, user registration, and session management
 * for the application. It integrates with Spring Security and depends on the user module
 * for user management operations.
 * </p>
 * 
 * <h2>Module Structure</h2>
 * <ul>
 *   <li><b>API:</b> {@code com.jotoai.voenix.shop.auth.api} - Public API for authentication operations and queries</li>
 *   <li><b>Web:</b> {@code com.jotoai.voenix.shop.auth.web} - REST controllers for authentication endpoints</li>
 *   <li><b>Internal:</b> Internal services and security implementations</li>
 *   <li><b>Config:</b> {@code com.jotoai.voenix.shop.auth.config} - Security configuration</li>
 * </ul>
 * 
 * <h2>Named Interfaces</h2>
 * <ul>
 *   <li>{@code api} - Public API for authentication operations and queries</li>
 * </ul>
 * 
 * <p>Other modules should explicitly depend on {@code auth::api} to access authentication functionality.</p>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Authentication",
        allowedDependencies = {"application", "user::api"}
)
package com.jotoai.voenix.shop.auth;