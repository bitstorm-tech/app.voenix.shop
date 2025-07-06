package com.jotoai.voenix.shop.users.dto

import com.jotoai.voenix.shop.users.entity.User
import java.time.OffsetDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
) {
    companion object {
        fun from(user: User): UserDto = UserDto(
            id = requireNotNull(user.id) { "User ID cannot be null when converting to DTO" },
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}