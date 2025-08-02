package com.jotoai.voenix.shop.domain.cart.exception

class CartNotFoundException : RuntimeException {
    constructor(userId: Long, isActiveCart: Boolean) : super(
        if (isActiveCart) {
            "Active cart not found for user: $userId"
        } else {
            "Cart not found for user: $userId"
        },
    )
}
