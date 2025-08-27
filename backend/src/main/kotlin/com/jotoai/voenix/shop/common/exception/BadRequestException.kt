package com.jotoai.voenix.shop.common.exception

class BadRequestException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
