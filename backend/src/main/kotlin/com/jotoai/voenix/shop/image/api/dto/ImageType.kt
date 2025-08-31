package com.jotoai.voenix.shop.image.api.dto

/**
 * Enum defining different image types with their specific configuration properties.
 */
enum class ImageType {
    PUBLIC,
    PRIVATE,
    PROMPT_EXAMPLE,
    PROMPT_SLOT_VARIANT_EXAMPLE,
    MUG_VARIANT_EXAMPLE,
    SHIRT_VARIANT_EXAMPLE,
    GENERATED;

    companion object {
        const val DEFAULT_MAX_SIZE = 10 * 1024 * 1024L // 10MB
        val DEFAULT_CONTENT_TYPES = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        val PNG_ONLY = listOf("image/png")
    }

    fun requiresWebPConversion(): Boolean = when (this) {
        PROMPT_EXAMPLE, PROMPT_SLOT_VARIANT_EXAMPLE, 
        MUG_VARIANT_EXAMPLE, SHIRT_VARIANT_EXAMPLE -> true
        else -> false
    }

    fun maxFileSize(): Long = DEFAULT_MAX_SIZE

    fun allowedContentTypes(): List<String> = 
        if (this == GENERATED) PNG_ONLY else DEFAULT_CONTENT_TYPES
}
