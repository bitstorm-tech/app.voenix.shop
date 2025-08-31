package com.jotoai.voenix.shop.user.api.exceptions

import com.jotoai.voenix.shop.application.ResourceNotFoundException

fun createUserNotFoundException(
    field: String,
    value: Any,
) = ResourceNotFoundException("User", field, value)
