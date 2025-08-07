package com.jotoai.voenix.shop.image.events

import java.time.LocalDateTime
import java.util.UUID

/**
 * Event published when an image is updated.
 */
data class ImageUpdatedEvent(
    val imageId: Long,
    val filename: String,
    val uuid: UUID?,
    val userId: Long?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)