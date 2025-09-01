package com.jotoai.voenix.shop.auth.internal.dto

import com.jotoai.voenix.shop.user.UserDto

data class SessionInfo(
    val authenticated: Boolean,
    val user: UserDto? = null,
    val roles: List<String> = emptyList(),
)
