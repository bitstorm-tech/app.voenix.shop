package com.jotoai.voenix.shop.openai.api

/**
 * Query service interface for OpenAI image generation operations.
 * This interface provides read-only operations and queries for OpenAI image data.
 */
interface OpenAIImageQueryService {
    // Currently, the OpenAI module doesn't have persistent data to query
    // This interface is created for consistency with other modules and future extensibility
    // If image generation history, prompts, or metadata need to be queried in the future,
    // the methods would be added here
}
