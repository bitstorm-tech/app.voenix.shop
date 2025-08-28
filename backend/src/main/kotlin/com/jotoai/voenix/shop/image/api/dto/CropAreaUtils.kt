package com.jotoai.voenix.shop.image.api.dto

object CropAreaUtils {
    fun createIfPresent(
        x: Double?,
        y: Double?,
        width: Double?,
        height: Double?,
    ): CropArea? =
        if (hasAllParameters(x, y, width, height)) {
            CropArea(x = x!!, y = y!!, width = width!!, height = height!!)
        } else {
            null
        }

    private fun hasAllParameters(
        x: Double?,
        y: Double?,
        width: Double?,
        height: Double?,
    ): Boolean = x != null && y != null && width != null && height != null
}
