package com.jotoai.voenix.shop.user.api.exceptions

import com.jotoai.voenix.shop.common.api.exception.ResourceNotFoundException

/**
 * Exception thrown when a user is not found.
 */
fun createUserNotFoundException(message: String) = ResourceNotFoundException(message)

fun createUserNotFoundException(
    field: String,
    value: Any,
) = ResourceNotFoundException("User", field, value)
