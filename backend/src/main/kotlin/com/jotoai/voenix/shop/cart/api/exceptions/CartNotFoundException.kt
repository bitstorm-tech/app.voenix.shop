package com.jotoai.voenix.shop.cart.api.exceptions

class CartNotFoundException : RuntimeException {
    constructor(userId: Long, isActiveCart: Boolean) : super(
        if (isActiveCart) {
            "Active cart not found for user: $userId"
        } else {
            "Cart not found for user: $userId"
        },
    )
}
