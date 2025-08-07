package com.jotoai.voenix.shop.image.events

import org.springframework.modulith.events.Externalized
import java.time.LocalDateTime
import java.util.UUID

/**
 * Event published when an image is updated.
 */
@Externalized
data class ImageUpdatedEvent(
    val imageId: Long,
    val filename: String,
    val uuid: UUID?,
    val userId: Long?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
