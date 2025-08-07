package com.jotoai.voenix.shop.image.events

import java.time.LocalDateTime
import java.util.UUID

/**
 * Event published when an image is deleted.
 */
data class ImageDeletedEvent(
    val imageId: Long,
    val filename: String,
    val uuid: UUID?,
    val userId: Long?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)