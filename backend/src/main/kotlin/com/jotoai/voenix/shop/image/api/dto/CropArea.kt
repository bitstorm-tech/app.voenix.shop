package com.jotoai.voenix.shop.image.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CropArea(
    @field:NotNull(message = "X coordinate is required")
    @field:Min(value = 0, message = "X coordinate must be non-negative")
    val x: Double,
    @field:NotNull(message = "Y coordinate is required")
    @field:Min(value = 0, message = "Y coordinate must be non-negative")
    val y: Double,
    @field:NotNull(message = "Width is required")
    @field:Min(value = 1, message = "Width must be at least 1")
    val width: Double,
    @field:NotNull(message = "Height is required")
    @field:Min(value = 1, message = "Height must be at least 1")
    val height: Double,
)
