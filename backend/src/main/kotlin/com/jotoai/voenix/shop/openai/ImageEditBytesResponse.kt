package com.jotoai.voenix.shop.openai

/**
 * Response containing raw image bytes from OpenAI API instead of stored filenames.
 * This allows the caller to handle storage using their preferred strategy.
 */
data class ImageEditBytesResponse(
    val imageBytes: List<ByteArray>,
)
