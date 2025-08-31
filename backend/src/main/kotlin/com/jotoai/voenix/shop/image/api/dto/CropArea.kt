package com.jotoai.voenix.shop.image.api.dto

import jakarta.validation.constraints.Min

data class CropArea(
    val x: Double,
    val y: Double,
    @field:Min(1) val width: Double,
    @field:Min(1) val height: Double,
) {
    companion object {
        fun fromNullable(x: Double?, y: Double?, width: Double?, height: Double?): CropArea? {
            return if (listOf(x, y, width, height).all { it != null }) {
                CropArea(x!!, y!!, width!!, height!!)
            } else null
        }
    }
}
