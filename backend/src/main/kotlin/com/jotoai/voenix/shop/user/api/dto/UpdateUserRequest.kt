package com.jotoai.voenix.shop.user.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:Email(message = "Email should be valid")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String? = null,
    @field:Size(max = 255, message = "First name must not exceed 255 characters")
    val firstName: String? = null,
    @field:Size(max = 255, message = "Last name must not exceed 255 characters")
    val lastName: String? = null,
    @field:Size(max = 255, message = "Phone number must not exceed 255 characters")
    val phoneNumber: String? = null,
    @field:Size(max = 255, message = "Password must not exceed 255 characters")
    val password: String? = null,
    @field:Size(max = 255, message = "One time password must not exceed 255 characters")
    val oneTimePassword: String? = null,
) {
    override fun toString(): String =
        "UpdateUserRequest(email=$email, firstName=$firstName, lastName=$lastName, " +
            "phoneNumber=$phoneNumber, password=${if (password != null) "[PROTECTED]" else null}, " +
            "oneTimePassword=${if (oneTimePassword != null) "[PROTECTED]" else null})"
}
