package com.jotoai.voenix.shop.users.dto

import java.time.OffsetDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)