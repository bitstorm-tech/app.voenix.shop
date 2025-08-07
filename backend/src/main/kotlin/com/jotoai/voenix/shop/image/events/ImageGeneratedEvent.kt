package com.jotoai.voenix.shop.image.events

import org.springframework.modulith.events.Externalized
import java.time.LocalDateTime

/**
 * Event published when an AI image is generated.
 */
@Externalized
data class ImageGeneratedEvent(
    val imageId: Long,
    val filename: String,
    val promptId: Long,
    val userId: Long?,
    val ipAddress: String?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
