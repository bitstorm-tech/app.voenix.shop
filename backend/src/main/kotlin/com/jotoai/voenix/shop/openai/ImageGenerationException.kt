package com.jotoai.voenix.shop.openai

/**
 * Exception thrown when image generation operations fail.
 */
class ImageGenerationException(
    message: String,
    cause: Throwable? = null,
) : OpenAIException(message, cause)
