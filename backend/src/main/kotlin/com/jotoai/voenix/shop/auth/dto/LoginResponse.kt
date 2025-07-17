package com.jotoai.voenix.shop.auth.dto

import com.jotoai.voenix.shop.users.dto.UserDto

data class LoginResponse(
    val user: UserDto,
    val sessionId: String,
    val roles: List<String>,
)
