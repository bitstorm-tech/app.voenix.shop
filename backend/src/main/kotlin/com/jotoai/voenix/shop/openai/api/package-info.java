/**
 * Public API for the OpenAI module.
 * <p>
 * This package contains all public interfaces and DTOs that other modules
 * can use to interact with the OpenAI module. The API is exposed as a named
 * interface "api" to ensure explicit dependencies.
 * </p>
 * <p>
 * Key components:
 * <ul>
 *   <li>{@link com.jotoai.voenix.shop.openai.api.OpenAIImageFacade} - Main facade for image operations</li>
 *   <li>{@link com.jotoai.voenix.shop.openai.api.OpenAIImageQueryService} - Query service for image data</li>
 *   <li>{@link com.jotoai.voenix.shop.openai.api.ImageGenerationStrategy} - Strategy interface for different backends</li>
 * </ul>
 * </p>
 */
@org.springframework.modulith.NamedInterface("api")
package com.jotoai.voenix.shop.openai.api;