/**
 * Prompt module for managing prompts, categories, subcategories, slot types, and slot variants.
 * This module follows Spring Modulith architecture patterns with clear separation between
 * API contracts and internal implementation details.
 *
 * <p>External modules should only depend on the API interfaces in the {@code api} package.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Prompt Management"
)
package com.jotoai.voenix.shop.prompt;