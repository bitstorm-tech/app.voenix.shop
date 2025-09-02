package com.jotoai.voenix.shop.user

import java.time.OffsetDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val roles: Set<String> = emptySet(),
    val isActive: Boolean = true,
    val passwordHash: String? = null,
)
