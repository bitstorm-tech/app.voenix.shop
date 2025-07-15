package com.jotoai.voenix.shop.openai.dto

import com.jotoai.voenix.shop.openai.dto.enums.ImageBackground
import com.jotoai.voenix.shop.openai.dto.enums.ImageQuality
import com.jotoai.voenix.shop.openai.dto.enums.ImageSize
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateImageEditRequest(
    @field:NotBlank(message = "Prompt is required")
    val prompt: String,
    @field:NotNull(message = "Background is required")
    val background: ImageBackground,
    @field:NotNull(message = "Quality is required")
    val quality: ImageQuality,
    @field:NotNull(message = "Size is required")
    val size: ImageSize,
    @field:Min(value = 1, message = "Number of images must be at least 1")
    @field:Max(value = 10, message = "Number of images cannot exceed 10")
    val n: Int = 1,
)
