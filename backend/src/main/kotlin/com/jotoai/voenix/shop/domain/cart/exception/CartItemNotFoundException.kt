package com.jotoai.voenix.shop.domain.cart.exception

class CartItemNotFoundException : RuntimeException {
    constructor(cartId: Long, itemId: Long) : super("Cart item $itemId not found in cart $cartId")
}
