/**
 * OpenAI module for managing AI-powered image generation and editing.
 * <p>
 * This module provides:
 * <ul>
 *   <li>AI image generation through concrete service implementations</li>
 *   <li>Image generation and editing operations</li> 
 *   <li>Strategy pattern for different image generation backends (OpenAI, test mode)</li>
 *   <li>Prompt testing functionality for AI image generation</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for OpenAI image operations and queries</li>
 *   <li>{@code web} - REST controllers for OpenAI admin operations</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code openai::api} to access OpenAI functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "OpenAI Integration"
)
package com.jotoai.voenix.shop.openai;