package com.jotoai.voenix.shop.openai.internal.exception

/**
 * Exception thrown when OpenAI configuration is invalid or missing.
 */
class OpenAIConfigurationException(
    message: String,
    cause: Throwable? = null,
) : OpenAIException(message, cause)
