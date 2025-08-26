package com.jotoai.voenix.shop.image.api.dto

object CropAreaUtils {
    fun createIfPresent(
        x: Double?,
        y: Double?,
        width: Double?,
        height: Double?,
    ): CropArea? {
        return if (x != null && y != null && width != null && height != null) {
            CropArea(x = x, y = y, width = width, height = height)
        } else {
            null
        }
    }
}