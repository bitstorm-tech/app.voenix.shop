package com.jotoai.voenix.shop.image.events

import java.time.LocalDateTime

/**
 * Event published when an AI image is generated.
 */
data class ImageGeneratedEvent(
    val imageId: Long,
    val filename: String,
    val promptId: Long,
    val userId: Long?,
    val ipAddress: String?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
