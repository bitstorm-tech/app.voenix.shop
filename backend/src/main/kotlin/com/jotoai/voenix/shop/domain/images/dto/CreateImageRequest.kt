package com.jotoai.voenix.shop.domain.images.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class CreateImageRequest(
    @field:NotNull(message = "Image type is required")
    val imageType: ImageType,
    @field:Valid
    val cropArea: CropArea? = null,
)
