package com.jotoai.voenix.shop.openai.api.exception

/**
 * Exception thrown when OpenAI configuration is invalid or missing.
 */
class OpenAIConfigurationException(
    message: String,
    cause: Throwable? = null,
) : OpenAIException(message, cause)
