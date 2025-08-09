package com.jotoai.voenix.shop.cart.api.exceptions

class CartItemNotFoundException : RuntimeException {
    constructor(cartId: Long, itemId: Long) : super("Cart item $itemId not found in cart $cartId")
}
