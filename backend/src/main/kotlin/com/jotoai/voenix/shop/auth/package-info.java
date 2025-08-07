/**
 * Authentication and authorization module.
 * <p>
 * This module provides authentication services, user registration, and session management
 * for the application. It integrates with Spring Security and depends on the user module
 * for user management operations.
 * </p>
 * 
 * <h2>Module Boundaries</h2>
 * <ul>
 *   <li><b>Services:</b> {@code com.jotoai.voenix.shop.auth.service} - Auth services</li>
 *   <li><b>DTOs:</b> {@code com.jotoai.voenix.shop.auth.dto} - Auth data transfer objects</li>
 *   <li><b>Config:</b> {@code com.jotoai.voenix.shop.auth.config} - Security configuration</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Authentication",
        allowedDependencies = {"common", "user::api", "user::api.dto"}
)
package com.jotoai.voenix.shop.auth;