package com.jotoai.voenix.shop.domain.cart.exception

class InsufficientStockException : RuntimeException {
    constructor(variantId: Long, requestedQuantity: Int, availableStock: Int) : super(
        "Insufficient stock for variant $variantId. Requested: $requestedQuantity, Available: $availableStock",
    )
}
