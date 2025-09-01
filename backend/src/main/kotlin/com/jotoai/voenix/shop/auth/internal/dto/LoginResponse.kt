package com.jotoai.voenix.shop.auth.internal.dto

import com.jotoai.voenix.shop.user.UserDto

data class LoginResponse(
    val user: UserDto,
    val sessionId: String,
    val roles: List<String>,
)
