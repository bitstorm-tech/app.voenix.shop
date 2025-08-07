package com.jotoai.voenix.shop.user.events

import org.springframework.modulith.events.Externalized

/**
 * Event published when a user is deleted.
 */
@Externalized
data class UserDeletedEvent(
    val userId: Long,
    val userEmail: String,
)
