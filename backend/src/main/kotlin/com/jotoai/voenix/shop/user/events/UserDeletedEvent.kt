package com.jotoai.voenix.shop.user.events

import org.springframework.modulith.events.Externalized
import java.time.OffsetDateTime

/**
 * Event published when a user is deleted.
 */
@Externalized("voenix.user::deleted")
data class UserDeletedEvent(
    val userId: Long,
    val userEmail: String,
    val deletedAt: OffsetDateTime,
    val isHardDelete: Boolean = false,
)
