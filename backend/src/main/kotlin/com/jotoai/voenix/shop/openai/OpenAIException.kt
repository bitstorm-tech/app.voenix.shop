package com.jotoai.voenix.shop.openai

/**
 * Base exception for all OpenAI module related errors.
 */
open class OpenAIException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
