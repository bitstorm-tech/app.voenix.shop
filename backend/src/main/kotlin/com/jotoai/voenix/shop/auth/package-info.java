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
 *   <li><b>Internal:</b> Internal services, security implementations, web controllers, and configuration</li>
 *   <ul>
 *     <li><b>Web:</b> {@code com.jotoai.voenix.shop.auth.internal.web} - REST controllers for authentication endpoints</li>
 *     <li><b>Config:</b> {@code com.jotoai.voenix.shop.auth.internal.config} - Security configuration</li>
 *     <li><b>Service:</b> {@code com.jotoai.voenix.shop.auth.internal.service} - Authentication service implementations</li>
 *     <li><b>Security:</b> {@code com.jotoai.voenix.shop.auth.internal.security} - Security components</li>
 *     <li><b>Exception:</b> {@code com.jotoai.voenix.shop.auth.internal.exception} - Exception handling</li>
 *   </ul>
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