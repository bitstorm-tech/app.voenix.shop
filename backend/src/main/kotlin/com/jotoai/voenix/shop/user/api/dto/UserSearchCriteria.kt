package com.jotoai.voenix.shop.user.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

/**
 * Search criteria for filtering users.
 * All fields are optional and will be combined with AND logic when multiple fields are specified.
 *
 * @param email Email address to search for (exact match)
 * @param emailContains Email address substring to search for (partial match)
 * @param firstName First name to search for (exact match)
 * @param firstNameContains First name substring to search for (partial match)
 * @param lastName Last name to search for (exact match)
 * @param lastNameContains Last name substring to search for (partial match)
 * @param phoneNumber Phone number to search for (exact match)
 * @param roles Set of role names - users must have ALL specified roles
 * @param hasAnyRole Set of role names - users must have AT LEAST ONE of specified roles
 * @param includeDeleted Whether to include soft-deleted users (default: false)
 */
data class UserSearchCriteria(
    @field:Email(message = "Email should be valid")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String? = null,
    @field:Size(max = 255, message = "Email search must not exceed 255 characters")
    val emailContains: String? = null,
    @field:Size(max = 255, message = "First name must not exceed 255 characters")
    val firstName: String? = null,
    @field:Size(max = 255, message = "First name search must not exceed 255 characters")
    val firstNameContains: String? = null,
    @field:Size(max = 255, message = "Last name must not exceed 255 characters")
    val lastName: String? = null,
    @field:Size(max = 255, message = "Last name search must not exceed 255 characters")
    val lastNameContains: String? = null,
    @field:Size(max = 255, message = "Phone number must not exceed 255 characters")
    val phoneNumber: String? = null,
    val roles: Set<String>? = null,
    val hasAnyRole: Set<String>? = null,
    val includeDeleted: Boolean = false,
)
