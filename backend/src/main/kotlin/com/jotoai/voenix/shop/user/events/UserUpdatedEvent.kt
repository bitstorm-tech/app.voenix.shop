package com.jotoai.voenix.shop.user.events

import com.jotoai.voenix.shop.user.api.dto.UserDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a user is updated.
 */
@Externalized
data class UserUpdatedEvent(
    val user: UserDto,
)
