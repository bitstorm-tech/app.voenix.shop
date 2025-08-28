package com.jotoai.voenix.shop.image.web.dto

import org.springframework.web.multipart.MultipartFile

/**
 * Form-backing object for multipart image generation requests.
 */
data class ImageGenerationForm(
    val image: MultipartFile,
    val promptId: Long,
    val cropX: Double? = null,
    val cropY: Double? = null,
    val cropWidth: Double? = null,
    val cropHeight: Double? = null,
)

