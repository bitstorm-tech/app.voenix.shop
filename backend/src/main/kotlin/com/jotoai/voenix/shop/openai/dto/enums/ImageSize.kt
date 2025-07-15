package com.jotoai.voenix.shop.openai.dto.enums

enum class ImageSize(
    val apiValue: String,
) {
    SQUARE_1024x1024("1024x1024"),
    LANDSCAPE_1536x1024("1536x1024"),
    PORTRAIT_1024x1536("1024x1536"),
}
