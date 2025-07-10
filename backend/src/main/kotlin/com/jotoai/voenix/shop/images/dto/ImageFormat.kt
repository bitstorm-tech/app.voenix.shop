package com.jotoai.voenix.shop.images.dto

enum class ImageFormat(
    val extension: String,
    val mimeType: String,
) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp"),
    GIF("gif", "image/gif"),
    ;

    companion object {
        fun fromExtension(extension: String): ImageFormat? {
            val ext = extension.lowercase().removePrefix(".")
            return entries.find { it.extension == ext || (it == JPEG && ext == "jpeg") }
        }

        fun fromMimeType(mimeType: String): ImageFormat? = entries.find { it.mimeType == mimeType }
    }
}
