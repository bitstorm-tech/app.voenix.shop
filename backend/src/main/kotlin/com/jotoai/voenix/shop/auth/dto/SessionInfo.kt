package com.jotoai.voenix.shop.auth.dto

import com.jotoai.voenix.shop.domain.users.dto.UserDto

data class SessionInfo(
    val authenticated: Boolean,
    val user: UserDto? = null,
    val roles: List<String> = emptyList(),
)
