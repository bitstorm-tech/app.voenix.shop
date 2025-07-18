package com.jotoai.voenix.shop.domain.openai.dto.enums

enum class ImageBackground(
    val apiValue: String,
) {
    TRANSPARENT("transparent"),
    OPAQUE("opaque"),
    AUTO("auto"),
}
