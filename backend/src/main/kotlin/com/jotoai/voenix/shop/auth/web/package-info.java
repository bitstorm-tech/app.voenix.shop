/**
 * Web layer for the authentication module.
 * <p>
 * Contains REST controllers that expose authentication functionality via HTTP endpoints.
 * This package serves as the web adapter for the authentication module, handling
 * HTTP requests and delegating to the authentication API services.
 * </p>
 * <p>
 * <strong>Note:</strong> This package contains infrastructure adapters and should not be 
 * accessed directly by other modules. Other modules should depend on the 
 * {@code auth::api} interface instead.
 * </p>
 * 
 * @since 1.0.0
 */
package com.jotoai.voenix.shop.auth.web;