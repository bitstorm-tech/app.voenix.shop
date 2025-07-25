package com.jotoai.voenix.shop.domain.openai.dto.enums

enum class ImageSize(
    val apiValue: String,
) {
    SQUARE_1024X1024("1024x1024"),
    LANDSCAPE_1536X1024("1536x1024"),
    PORTRAIT_1024X1536("1024x1536"),
}
