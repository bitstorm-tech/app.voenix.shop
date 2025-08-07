package com.jotoai.voenix.shop.user.events

import com.jotoai.voenix.shop.user.api.dto.UserDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new user is created.
 */
@Externalized
data class UserCreatedEvent(
    val user: UserDto,
)
