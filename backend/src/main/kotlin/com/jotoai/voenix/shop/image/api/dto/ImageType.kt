package com.jotoai.voenix.shop.image.api.dto

/**
 * Enum defining different image types with their specific configuration properties.
 * Each image type has its own validation rules and processing requirements.
 */
enum class ImageType(
    val requiresWebPConversion: Boolean,
    val maxFileSize: Long,
    val allowedContentTypes: List<String>,
) {
    PUBLIC(
        requiresWebPConversion = false,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    PRIVATE(
        requiresWebPConversion = false,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    PROMPT_EXAMPLE(
        requiresWebPConversion = true,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    PROMPT_SLOT_VARIANT_EXAMPLE(
        requiresWebPConversion = true,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    MUG_VARIANT_EXAMPLE(
        requiresWebPConversion = true,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    SHIRT_VARIANT_EXAMPLE(
        requiresWebPConversion = true,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp"),
    ),
    GENERATED(
        requiresWebPConversion = false,
        maxFileSize = 10 * 1024 * 1024L, // 10MB
        allowedContentTypes = listOf("image/png"), // Generated images are typically PNG
    ),
    ;

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
