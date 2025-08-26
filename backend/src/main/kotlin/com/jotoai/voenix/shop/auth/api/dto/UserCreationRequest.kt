package com.jotoai.voenix.shop.auth.api.dto

data class UserCreationRequest(
    val email: String,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val roleNames: Set<String> = setOf("USER"),
)
