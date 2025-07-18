package com.jotoai.voenix.shop.domain.images.dto

data class ConversionOptions(
    val quality: Float = 0.85f,
    val preserveMetadata: Boolean = false,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null,
) {
    init {
        require(quality in 0.0f..1.0f) { "Quality must be between 0.0 and 1.0" }
        maxWidth?.let { require(it > 0) { "Max width must be positive" } }
        maxHeight?.let { require(it > 0) { "Max height must be positive" } }
    }
}
