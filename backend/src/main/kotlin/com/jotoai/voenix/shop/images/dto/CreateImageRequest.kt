package com.jotoai.voenix.shop.images.dto

import jakarta.validation.constraints.NotNull

data class CreateImageRequest(
    @field:NotNull(message = "Image type is required")
    val imageType: ImageType,
)
