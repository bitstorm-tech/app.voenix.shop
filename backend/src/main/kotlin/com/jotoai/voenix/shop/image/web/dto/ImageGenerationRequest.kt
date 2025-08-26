package com.jotoai.voenix.shop.image.web.dto

data class ImageGenerationRequest(
    val promptId: Long,
    val cropX: Double? = null,
    val cropY: Double? = null,
    val cropWidth: Double? = null,
    val cropHeight: Double? = null,
)