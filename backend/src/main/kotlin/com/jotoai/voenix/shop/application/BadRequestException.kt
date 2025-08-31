package com.jotoai.voenix.shop.application

/**
 * Exception for invalid request scenarios.
 * Used when request data is malformed or violates business rules.
 */
class BadRequestException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
