package com.jotoai.voenix.shop.image.api.dto

/**
 * Enum defining different image types with their specific configuration properties.
 * Each image type has its own validation rules and processing requirements.
 */
enum class ImageType(
    val requiresWebPConversion: Boolean = false,
) {
    PUBLIC,
    PRIVATE,
    PROMPT_EXAMPLE(requiresWebPConversion = true),
    PROMPT_SLOT_VARIANT_EXAMPLE(requiresWebPConversion = true),
    MUG_VARIANT_EXAMPLE(requiresWebPConversion = true),
    SHIRT_VARIANT_EXAMPLE(requiresWebPConversion = true),
    GENERATED,
    ;

    companion object {
        const val DEFAULT_MAX_SIZE = 10 * 1024 * 1024L // 10MB
        val DEFAULT_CONTENT_TYPES = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        val PNG_ONLY = listOf("image/png")
    }

    val maxFileSize: Long
        get() = DEFAULT_MAX_SIZE

    val allowedContentTypes: List<String>
        get() = if (this == GENERATED) PNG_ONLY else DEFAULT_CONTENT_TYPES

    /**
     * Returns the appropriate file extension for this image type.
     * Takes into account WebP conversion requirements.
     */
    fun getFileExtension(originalFilename: String): String =
        if (requiresWebPConversion) {
            ".webp"
        } else {
            val lastDotIndex = originalFilename.lastIndexOf('.')
            if (lastDotIndex > 0) originalFilename.substring(lastDotIndex) else ""
        }
}
