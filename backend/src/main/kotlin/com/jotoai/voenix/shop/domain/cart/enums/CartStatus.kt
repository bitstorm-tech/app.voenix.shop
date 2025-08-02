package com.jotoai.voenix.shop.domain.cart.enums

enum class CartStatus {
    ACTIVE, // Cart is currently active and being used
    ABANDONED, // Cart was abandoned by the user
    CONVERTED, // Cart was converted to an order
}
