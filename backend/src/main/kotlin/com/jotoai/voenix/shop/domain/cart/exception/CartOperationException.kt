package com.jotoai.voenix.shop.domain.cart.exception

class CartOperationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
